package com.eden.alarmplus.logic.internal

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.eden.alarmplus.alert.AlertLogic
import com.eden.alarmplus.alert.AlertService
import com.eden.alarmplus.data.alarms.repository.StateRepository
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.group.feature.DayOfWeekFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.ScheduleBeginFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.ScheduleEndFeature
import com.eden.alarmplus.data.alarms.structure.state.StateType
import com.eden.alarmplus.util.time.isTimeRightBeforeAndNotAnchor
import com.eden.alarmplus.util.time.isTimeWithinTimeSpan
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

class StateProcessor(
    private val context: Context,
    private val stateRepository: StateRepository,
    private val wakeScheduler: WakeScheduler,
    private val dismissWindow: Duration
) {

    companion object {

        object Lock {

            data class Handle(
                val index: Long,
                val mode: HandleMode,
            )

            enum class HandleMode {
                DISMISS,
                INIT,
                ITERATE,
            }

            private const val UPDATE_BIT = 0
            private const val BLOCKING_BIT = 1
            enum class LockMode(val mask: Int) {
                IDLE(0 shl UPDATE_BIT + 0 shl BLOCKING_BIT),
                UPDATE_BLOCKING(1 shl UPDATE_BIT + 1 shl BLOCKING_BIT),
                UPDATE_SURPASSABLE(1 shl UPDATE_BIT + 0 shl BLOCKING_BIT),
            }
            fun LockMode.isUpdating(): Boolean = (this.mask and (1 shl UPDATE_BIT)) != 0
            fun LockMode.isBlocking(): Boolean = (this.mask and (1 shl BLOCKING_BIT)) != 0


            private var mode = LockMode.IDLE
            private var latestIndex = 0L

            fun tryGetDismissHandle() = synchronized(this@Lock) {
                if (mode.isBlocking())
                    return@synchronized null

                mode = LockMode.UPDATE_BLOCKING
                return@synchronized Handle(
                    ++latestIndex,
                    mode = HandleMode.DISMISS,
                )
            }

            fun tryGetInitHandle() = synchronized(this@Lock) {
                if (mode.isBlocking())
                    return@synchronized null

                mode = LockMode.UPDATE_BLOCKING
                return@synchronized Handle(
                    ++latestIndex,
                    mode = HandleMode.INIT,
                )
            }

            fun tryGetIterateHandle() = synchronized(this@Lock) {
                if (mode.isBlocking())
                    return@synchronized null

                mode = LockMode.UPDATE_SURPASSABLE
                return@synchronized Handle(
                    ++latestIndex,
                    mode = HandleMode.ITERATE,
                )
            }

            fun returnHandle(handle: Handle): Unit = synchronized(this@Lock) {
                if (isValidImplementation(handle)) {
                    latestIndex++
                    mode = LockMode.IDLE
                }
            }

            private fun isValidImplementation(handle: Handle): Boolean {
                if (handle.index != latestIndex)
                    return false

                when (handle.mode) {
                    HandleMode.INIT -> if (this@Lock.mode != LockMode.UPDATE_BLOCKING) return false
                    HandleMode.DISMISS -> if (this@Lock.mode != LockMode.UPDATE_BLOCKING) return false
                    HandleMode.ITERATE -> if (this@Lock.mode != LockMode.UPDATE_SURPASSABLE) return false
                }

                return true
            }


            fun isValid(handle: Handle) = synchronized(this@Lock) { isValidImplementation(handle) }
        }
    }

    private class InvalidatedStateException: RuntimeException()

    /**
     * Use when state is not guaranteed (i.e. time zone changes or from restart)
     * @param dateTime the lower bound exclusive to process
     */
    suspend fun initializeState(dateTime: LocalDateTime) {
        var handle: Lock.Handle
        while (true) {
            val result = Lock.tryGetInitHandle()
            if (result != null) {
                handle = result
                break
            }
            delay(10.milliseconds)
        }

        try {
            constructState(handle, dateTime)

            scheduleWake(handle, dateTime)
        }
        catch (_: InvalidatedStateException) {
            Log.i("$javaClass", "This initialization block is no longer valid, exiting early.")
        }
        finally {
            Lock.returnHandle(handle)
        }
    }

    /**
     * Assume either there were previous [initializeState] or [iterateState] ran
     * Assume time zone remains unchanged, within time zone jump is acceptable (i.e. summer to winter)
     * @param dateTime to process
     */
    suspend fun iterateState(dateTime: LocalDateTime) {

        val handle = Lock.tryGetIterateHandle()
        if (handle == null) {
            Log.i("${this::class.java}", "Initialization block in progress, exiting this iterate block early.")
            return
        }

        try {
            processCurrent(handle, dateTime)

            scheduleWake(handle, dateTime)
        }
        catch (_: InvalidatedStateException) {
            Log.i("$javaClass", "This iterate block is no longer valid, exiting early.")
        }
        finally {
            Lock.returnHandle(handle)
        }

    }

    suspend fun dismissGroup(groupUuid: Uuid) {
        var handle: Lock.Handle
        while (true) {
            val result = Lock.tryGetDismissHandle()
            if (result != null) {
                handle = result
                break
            }
            delay(10.milliseconds)
        }

        try {
            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            Log.i("$javaClass", "Dismissing group '$groupUuid'.")
            // return if null (invalidated by changes, a new run should be triggered)
            val currentState = stateRepository.getState(groupUuid) ?: throw InvalidatedStateException()

            val newState = when (currentState) {
                StateType.DISABLED -> currentState
                StateType.ENABLED_PENDING -> currentState
                StateType.ENABLED_PRIMED -> StateType.ENABLED_DISMISSED
                StateType.ENABLED_ACTIVE -> StateType.ENABLED_DISMISSED
                StateType.ENABLED_DISMISSED -> currentState
                StateType.ENABLED_IDLE -> currentState
                StateType.ENABLED_UNDEFINED -> {
                    Log.w("$javaClass", "Dismissing group $groupUuid with existing state Undefined.")
                    StateType.ENABLED_DISMISSED
                }
            }

            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            stateRepository.updateState(groupUuid, newState)
        }
        finally {
            Lock.returnHandle(handle)
        }
    }

    /**
     * @param handle to check if valid
     * @param dateTime to evaluate
     * @throws InvalidatedStateException when handle is found to be out of date
     */
    private suspend fun constructState(handle: Lock.Handle, dateTime: LocalDateTime) {

        val forewarnGroups = mutableListOf<GroupInfo>()

        val day = dateTime.dayOfWeek
        val dayBefore = day.minus(1)
        val dayAfter = day.plus(1)
        val date = dateTime.toLocalDate()
        val dateBefore = date.minusDays(1)
        val dateAfter = date.plusDays(1)
        val time = dateTime.toLocalTime()

        for ((info, alarms) in stateRepository.getAllGroups()) {

            val dayOfWeekFlags = info.getFeature<DayOfWeekFeature>()?.flags ?: DayOfWeekFlags.ALL

            val beginDate = info.getFeature<ScheduleBeginFeature>()?.date
            val endDate = info.getFeature<ScheduleEndFeature>()?.date
            fun LocalDate.allowed() =
                (beginDate == null || !this.isBefore(beginDate)) &&
                (endDate == null || !this.isAfter(endDate))

            val allowYesterday = dayOfWeekFlags.any(dayBefore) && dateBefore.allowed()
            val allowToday = dayOfWeekFlags.any(day) && date.allowed()
            val allowTomorrow = dayOfWeekFlags.any(dayAfter) && dateAfter.allowed()

            val inDuration: Boolean = isTimeWithinTimeSpan(
                alarms.map { it.info.time },
                time,
                allowToday,
                allowYesterday,
            )
            val scheduleUpcoming = beginDate != null && !beginDate.isBefore(date)
            val scheduleGone = endDate != null && endDate.isBefore(date)
            val inDismissableWindow: Boolean = alarms.isNotEmpty() && isTimeRightBeforeAndNotAnchor(
                alarms.first().info.time,
                dismissWindow,
                time,
                allowToday,
                allowTomorrow,
            )

            val currentState = info.state
            val newState = when (currentState) {
                StateType.DISABLED ->
                    currentState
                StateType.ENABLED_UNDEFINED ->
                    if (inDuration) StateType.ENABLED_ACTIVE
                    else if (scheduleUpcoming) StateType.ENABLED_PENDING
                    else if (scheduleGone) StateType.DISABLED
                    else StateType.ENABLED_IDLE
                StateType.ENABLED_PENDING ->
                    if (inDuration) StateType.ENABLED_ACTIVE
                    else if (scheduleGone) StateType.DISABLED
                    else if (scheduleUpcoming) StateType.ENABLED_PENDING
                    else StateType.ENABLED_IDLE
                StateType.ENABLED_PRIMED ->
                    if (inDuration) StateType.ENABLED_ACTIVE
                    else if (scheduleGone) StateType.DISABLED
                    else currentState
                StateType.ENABLED_ACTIVE ->
                    if (inDuration) StateType.ENABLED_ACTIVE
                    else if (scheduleGone) StateType.DISABLED
                    else StateType.ENABLED_IDLE
                StateType.ENABLED_DISMISSED ->
                    if (inDuration) StateType.ENABLED_DISMISSED
                    else if (scheduleGone) StateType.DISABLED
                    else StateType.ENABLED_IDLE
                StateType.ENABLED_IDLE ->
                    if (inDuration) StateType.ENABLED_ACTIVE
                    else if (scheduleGone) StateType.DISABLED
                    else currentState
            }.let { when (it) {
                StateType.DISABLED -> it
                StateType.ENABLED_PENDING -> if (inDismissableWindow) StateType.ENABLED_PRIMED else it
                StateType.ENABLED_PRIMED -> it
                StateType.ENABLED_ACTIVE -> it
                StateType.ENABLED_DISMISSED -> it
                StateType.ENABLED_IDLE -> if (inDismissableWindow) StateType.ENABLED_PRIMED else it
                StateType.ENABLED_UNDEFINED -> it
            } }

            if (inDismissableWindow && !inDuration)
                forewarnGroups.add(info)

            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            stateRepository.updateState(info.uuid, newState)
        }

        context.startService(
            Intent(context, AlertService::class.java).apply {
                action = AlertLogic.ACTION_CLEAR_FOREWARN
            }
        )

        for (info in forewarnGroups) {
            context.startService(
                Intent(context, AlertService::class.java).apply {
                    action = AlertLogic.ACTION_FOREWARN
                    putExtra(AlertLogic.EXTRA_GROUP_INFO, info)
                }
            )
        }
    }

    /**
     * @param handle to check if valid
     * @param dateTime to schedule from
     * @throws InvalidatedStateException when handle is found to be out of date
     */
    private suspend fun processCurrent(handle: Lock.Handle, dateTime: LocalDateTime) {
        if (!Lock.isValid(handle)) throw InvalidatedStateException()

        val day = dateTime.dayOfWeek
        val dayBefore = day.minus(1)
        val dayAfter = day.plus(1)
        val date = dateTime.toLocalDate()
        val dateBefore = date.minusDays(1)
        val dateAfter = date.plusDays(1)
        val time = dateTime.toLocalTime()

        for ((info, alarms) in stateRepository.getAllGroups()) {

            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            // return if null (invalidated by changes, a new run should be triggered)
            var currentState = info.state
            when (currentState) {
                StateType.DISABLED -> continue
                StateType.ENABLED_UNDEFINED -> continue
                else -> {}
            }
            var stateUpdated = false

            val dayOfWeekFlags = info.getFeature<DayOfWeekFeature>()?.flags ?: DayOfWeekFlags.ALL

            val beginDate = info.getFeature<ScheduleBeginFeature>()?.date
            val endDate = info.getFeature<ScheduleEndFeature>()?.date
            fun LocalDate.allowed() =
                (beginDate == null || !this.isBefore(beginDate)) &&
                (endDate == null || !this.isAfter(endDate))

            val allowYesterday = dayOfWeekFlags.any(dayBefore) && dateBefore.allowed()
            val allowToday = dayOfWeekFlags.any(day) && date.allowed()
            val allowTomorrow = dayOfWeekFlags.any(dayAfter) && dateAfter.allowed()

            val groupStartTime = alarms.first().info.time

            val primedAtTime = isTimeRightBeforeAndNotAnchor(
                alarms.first().info.time,
                dismissWindow,
                time,
                allowToday,
                allowTomorrow,
            )

            if (primedAtTime) {
                val shouldNotify = when (currentState) {
                    StateType.ENABLED_PENDING -> true
                    StateType.ENABLED_PRIMED -> false
                    StateType.ENABLED_ACTIVE -> false
                    StateType.ENABLED_DISMISSED -> false
                    StateType.ENABLED_IDLE -> true
                }

                currentState = when (currentState) {
                    StateType.ENABLED_PENDING -> StateType.ENABLED_PRIMED
                    StateType.ENABLED_PRIMED -> currentState
                    StateType.ENABLED_ACTIVE -> currentState
                    StateType.ENABLED_DISMISSED -> currentState
                    StateType.ENABLED_IDLE -> StateType.ENABLED_PRIMED
                }
                stateUpdated = true

                if (shouldNotify)
                    context.startService(
                        Intent(context, AlertService::class.java).apply {
                            action = AlertLogic.ACTION_FOREWARN
                            putExtra(AlertLogic.EXTRA_GROUP_INFO, info)
                        }
                    )
            }

            fun AlarmData.filterByDayOfWeek(groupStartTime: LocalTime): AlarmData? =
                if (allowYesterday && allowToday) this
                else if (allowYesterday) { if (this.info.time.isBefore(groupStartTime)) this else null }
                else if (allowToday)  { if (!this.info.time.isBefore(groupStartTime)) this else null }
                else null

            val alarmAtTime = alarms
                .lastOrNull { it.info.time == time }
                ?.filterByDayOfWeek(alarms.first().info.time)

            if (alarmAtTime != null) {

                val inDismissableWindow = isTimeRightBeforeAndNotAnchor(
                    groupStartTime,
                    dismissWindow,
                    time,
                    allowToday,
                    allowTomorrow
                )
                val isLastInGroup = alarms.last().info.time == time
                val onEndDate = endDate != null && date == endDate

                val shouldNotify = when (currentState) {
                    StateType.DISABLED -> false
                    StateType.ENABLED_PENDING -> true
                    StateType.ENABLED_PRIMED -> true
                    StateType.ENABLED_ACTIVE -> true
                    StateType.ENABLED_DISMISSED -> false
                    StateType.ENABLED_IDLE -> true
                    StateType.ENABLED_UNDEFINED -> {
                        Log.w("$javaClass", "Updating group ${info.uuid} with existing state Undefined.")
                        true
                    }
                }

                currentState = when (currentState) {
                    StateType.DISABLED ->
                        currentState
                    StateType.ENABLED_PENDING,
                    StateType.ENABLED_PRIMED,
                    StateType.ENABLED_ACTIVE,
                    StateType.ENABLED_IDLE->
                        if (isLastInGroup && onEndDate) StateType.DISABLED
                        else if (isLastInGroup) StateType.ENABLED_IDLE
                        else StateType.ENABLED_ACTIVE
                    StateType.ENABLED_DISMISSED ->
                        if (isLastInGroup && onEndDate) StateType.DISABLED
                        else if (isLastInGroup) StateType.ENABLED_IDLE
                        else StateType.ENABLED_DISMISSED
                    StateType.ENABLED_UNDEFINED -> {
                        Log.w("$javaClass", "Updating group ${info.uuid} with existing state Undefined.")
                        if (isLastInGroup && onEndDate) StateType.DISABLED
                        else if (isLastInGroup) StateType.ENABLED_IDLE
                        else StateType.ENABLED_ACTIVE
                    }
                }.let { when (it) {
                    StateType.DISABLED -> it
                    StateType.ENABLED_PENDING -> if (inDismissableWindow) StateType.ENABLED_PRIMED else it
                    StateType.ENABLED_PRIMED -> it
                    StateType.ENABLED_ACTIVE -> it
                    StateType.ENABLED_DISMISSED -> it
                    StateType.ENABLED_IDLE -> if (inDismissableWindow) StateType.ENABLED_PRIMED else it
                    StateType.ENABLED_UNDEFINED -> {
                        Log.w("$javaClass", "Updating group ${info.uuid} with existing state Undefined.")
                        it
                    }
                } }
                stateUpdated = true

                // Show alarm
                if (shouldNotify)
                    ContextCompat.startForegroundService(context,
                        Intent(context, AlertService::class.java).apply {
                            action = AlertLogic.ACTION_ALERT
                            putExtra(AlertLogic.EXTRA_GROUP_INFO, info)
                            putExtra(AlertLogic.EXTRA_ALARM_DATA, alarmAtTime)
                            putExtra(AlertLogic.EXTRA_IS_LAST_IN_GROUP, isLastInGroup)
                        }
                    )
            }

            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            if (stateUpdated)
                stateRepository.updateState(info.uuid, currentState)
        }
    }

    /**
     * @param handle to check if valid
     * @param dateTime to schedule from
     * @throws InvalidatedStateException when handle is found to be out of date
     */
    private suspend fun scheduleWake(handle: Lock.Handle, dateTime: LocalDateTime) {
        if (!Lock.isValid(handle)) throw InvalidatedStateException()
        // Repeated call from race condition should not be an issue
        wakeScheduler.tryScheduleWake(fromDateTime = dateTime)
    }
}