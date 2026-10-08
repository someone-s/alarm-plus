package com.eden.alarmplus.logic.internal

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.eden.alarmplus.alert.AlertLogic
import com.eden.alarmplus.alert.AlertService
import com.eden.alarmplus.data.alarms.repository.StateRepository
import com.eden.alarmplus.data.alarms.structure.state.StateData
import com.eden.alarmplus.util.time.isTimeRightBeforeAndNotAnchor
import com.eden.alarmplus.util.time.isTimeWithinTimeSpan
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
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

            val newState = when (currentState.type) {
                StateData.Type.ACTIVE -> currentState.toDismissed()
                StateData.Type.DISMISSED -> currentState
                StateData.Type.IDLE -> currentState
                StateData.Type.PRIMED -> currentState.toDismissed()
                StateData.Type.UNDEFINED -> {
                    Log.w("$javaClass", "Dismissing group $groupUuid with existing state Undefined.")
                    currentState.toDismissed()
                }
            }

            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            stateRepository.updateState(newState)
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
        val day = dateTime.dayOfWeek
        val time = dateTime.toLocalTime()
        for ((info, alarms, dayOfWeekFlags) in stateRepository.getAllGroups()) {

            val inDuration: Boolean = isTimeWithinTimeSpan(
                alarms.map { it.info.time },
                time,
                allowToday = dayOfWeekFlags.any(day),
                allowYesterday = dayOfWeekFlags.any(day.minus(1)),
            )
            val inDismissableWindow: Boolean = alarms.isNotEmpty() && isTimeRightBeforeAndNotAnchor(
                alarms.first().info.time,
                dismissWindow,
                time,
                allowToday = dayOfWeekFlags.any(day),
                allowTomorrow = dayOfWeekFlags.any(day.plus(1)),
            )

            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            // return if null (invalidated by changes, a new run should be triggered)
            val currentState = stateRepository.getState(info.uuid) ?: throw InvalidatedStateException()

            val newState =
                if (info.enabled) {
                    val intermediateState = when (currentState.type) {
                        StateData.Type.ACTIVE -> if (inDuration) currentState else currentState.toIdle()
                        StateData.Type.DISMISSED -> if (inDuration) currentState else currentState.toIdle()
                        StateData.Type.IDLE -> if (inDuration) currentState.toPrimed().toActive() else currentState
                        StateData.Type.PRIMED -> if (inDuration) currentState.toActive() else currentState
                        StateData.Type.UNDEFINED -> if (inDuration) currentState.toActive() else currentState.toIdle()
                    }
                    when (intermediateState.type) {
                        StateData.Type.ACTIVE -> intermediateState
                        StateData.Type.DISMISSED -> intermediateState
                        StateData.Type.IDLE -> if (inDismissableWindow) intermediateState.toPrimed() else intermediateState
                        StateData.Type.PRIMED -> intermediateState
                        StateData.Type.UNDEFINED -> intermediateState
                    }
                }
                else
                    when (currentState.type) {
                        StateData.Type.ACTIVE -> currentState.toIdle()
                        StateData.Type.DISMISSED -> currentState.toIdle()
                        StateData.Type.IDLE -> currentState
                        StateData.Type.PRIMED -> currentState.toDismissed().toIdle()
                        StateData.Type.UNDEFINED -> currentState.toIdle()
                    }



            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            stateRepository.updateState(newState)
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
        val time = dateTime.toLocalTime()
        val alarmsAtTime = stateRepository.getAlarmsAtTime(day, time, dismissWindow)

        for (alarmAtTime in alarmsAtTime) {

            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            // return if null (invalidated by changes, a new run should be triggered)
            val currentState = stateRepository.getState(alarmAtTime.group.uuid) ?: throw InvalidatedStateException()

            val shouldNotify = when (currentState.type) {
                StateData.Type.ACTIVE -> true
                StateData.Type.DISMISSED -> false
                StateData.Type.IDLE -> true
                StateData.Type.PRIMED -> true
                StateData.Type.UNDEFINED -> {
                    Log.w("$javaClass", "Updating group ${alarmAtTime.group.uuid} with existing state Undefined.")
                    true
                }
            }

            val intermediateState = when (currentState.type) {
                StateData.Type.ACTIVE -> if (alarmAtTime.isLastInGroup) currentState.toIdle() else currentState
                StateData.Type.DISMISSED -> if (alarmAtTime.isLastInGroup) currentState.toIdle() else currentState
                StateData.Type.IDLE -> if (alarmAtTime.isLastInGroup) currentState else currentState.toPrimed().toActive()
                StateData.Type.PRIMED -> if (alarmAtTime.isLastInGroup) currentState.toActive().toIdle() else currentState.toActive()
                StateData.Type.UNDEFINED -> {
                    Log.w("$javaClass", "Updating group ${alarmAtTime.group.uuid} with existing state Undefined.")
                    if (alarmAtTime.isLastInGroup) currentState.toIdle() else currentState.toActive()
                }
            }

            val newState = when (intermediateState.type) {
                StateData.Type.ACTIVE -> intermediateState
                StateData.Type.DISMISSED -> intermediateState
                StateData.Type.IDLE -> if (alarmAtTime.inDismissableWindow) intermediateState.toPrimed() else intermediateState
                StateData.Type.PRIMED -> intermediateState
                StateData.Type.UNDEFINED -> {
                    Log.w("$javaClass", "Updating group ${alarmAtTime.group.uuid} with existing state Undefined.")
                    intermediateState
                }
            }

            // Show alarm
            if (shouldNotify)
                ContextCompat.startForegroundService(context,
                    Intent(context, AlertService::class.java).apply {
                        action = AlertLogic.ACTION_ALERT
                        putExtra(AlertLogic.EXTRA_GROUP_INFO, alarmAtTime.group)
                        putExtra(AlertLogic.EXTRA_ALARM_DATA, alarmAtTime.alarm)
                        putExtra(AlertLogic.EXTRA_IS_LAST_IN_GROUP, alarmAtTime.isLastInGroup)
                    }
                )

            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            stateRepository.updateState(newState)
        }

        val primedAtTime = stateRepository.getPrimedAtTime(day, time, dismissWindow)

        for (groupUuid in primedAtTime) {
            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            // return if null (invalidated by changes, a new run should be triggered)
            val currentState = stateRepository.getState(groupUuid) ?: throw InvalidatedStateException()

            val newState = when (currentState.type) {
                StateData.Type.ACTIVE -> currentState
                StateData.Type.DISMISSED -> currentState
                StateData.Type.IDLE -> currentState.toPrimed()
                StateData.Type.PRIMED -> currentState
                StateData.Type.UNDEFINED -> {
                    Log.w("$javaClass", "Updating group ${groupUuid} with existing state Undefined.")
                    currentState
                }
            }
            if (!Lock.isValid(handle)) throw InvalidatedStateException()
            stateRepository.updateState(newState)
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