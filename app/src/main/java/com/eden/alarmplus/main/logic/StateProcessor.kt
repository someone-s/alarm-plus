package com.eden.alarmplus.main.logic

import android.util.Log
import com.eden.alarmplus.data.repository.StateRepository
import com.eden.alarmplus.main.logic.StateProcessor.Companion.Lock.isValid
import com.eden.alarmplus.structure.state.StateData
import java.time.LocalDateTime

class StateProcessor(
    private val stateRepository: StateRepository,
    private val wakeScheduler: WakeScheduler
) {

    companion object {
        object Lock {

            data class Handle(
                val index: Long,
                val isInit: Boolean,
            )

            private var initializing = false
            private var latestIndex = 0L

            fun getInitHandle() = synchronized(this@Lock) {
                initializing = true
                return@synchronized Handle(
                    ++latestIndex,
                    isInit = true,
                )
            }

            fun returnInitHandle(handle: Handle): Unit = synchronized(this@Lock) {
                if (isValidImplementation(handle)) {
                    initializing = false
                    latestIndex++
                }
            }

            fun tryGetIterateHandle() = synchronized(this@Lock) {
                if (initializing)
                    return@synchronized null

                return@synchronized Handle(
                    ++latestIndex,
                    isInit = false,
                )
            }

            private fun isValidImplementation(handle: Handle): Boolean {
                if (!handle.isInit && initializing)
                    return false

                if (handle.index != latestIndex)
                    return false

                return true
            }

            fun Handle.isValid() = synchronized(this@Lock) { isValidImplementation(this) }
        }
    }

    private class InvalidatedStateException: RuntimeException()

    /**
     * Use when state is not guaranteed (i.e. time zone changes or from restart)
     * @param dateTime the lower bound exclusive to process
     */
    suspend fun initializeState(dateTime: LocalDateTime) {

        val handle = Lock.getInitHandle()

        try {
            constructState(handle, dateTime)

            processCurrent(handle, dateTime)

            scheduleWake(handle, dateTime)
        }
        catch (_: InvalidatedStateException) {
            Log.i("$javaClass", "This initialization block is no longer valid, exiting early.")
        }
        finally {
            Lock.returnInitHandle(handle)
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

    }

    /**
     * @param handle to check if valid
     * @param dateTime to evaluate
     * @throws InvalidatedStateException when handle is found to be out of date
     */
    private suspend fun constructState(handle: Lock.Handle, dateTime: LocalDateTime) {
        val time = dateTime.toLocalTime()
        for (group in stateRepository.getAllGroups()) {

            val inDuration: Boolean

            if (group.alarms.isEmpty()) {
                inDuration = false
            } else if (group.alarms.size == 1) {
                inDuration = time.equals(group.alarms.first())
            } else {
                val startTime = group.alarms.first().info.time
                val endTime = group.alarms.last().info.time

                val isWrapAround = startTime.isAfter(endTime)

                val isAfterOrAtStart = time.isAfter(startTime) || time.equals(startTime)
                val isAfterOrAtEnd = time.isAfter(endTime) || time.equals(endTime)

                inDuration =
                    if (isWrapAround) isAfterOrAtStart == isAfterOrAtEnd
                    else isAfterOrAtStart != isAfterOrAtEnd
            }

            if (!handle.isValid()) throw InvalidatedStateException()
            // return if null (invalidated by changes, a new run should be triggered)
            val currentState = stateRepository.getState(group.info.uuid) ?: throw InvalidatedStateException()

            val newState = when (currentState) {
                is StateData.Active -> if (inDuration) currentState else currentState.toIdle()
                is StateData.Dismissed -> if (inDuration) currentState else currentState.toIdle()
                is StateData.Idle -> if (inDuration) currentState.toActive() else currentState
                is StateData.Undefined -> if (inDuration) currentState.toActive() else currentState.toIdle()
            }

            if (!handle.isValid()) throw InvalidatedStateException()
            stateRepository.updateState(newState)
        }
    }

    /**
     * @param handle to check if valid
     * @param dateTime to schedule from
     * @throws InvalidatedStateException when handle is found to be out of date
     */
    private suspend fun processCurrent(handle: Lock.Handle, dateTime: LocalDateTime) {
        if (!handle.isValid()) throw InvalidatedStateException()

        val time = dateTime.toLocalTime()
        val alarmsAtTime = stateRepository.getAlarmsAtTime(time)

        for (alarmAtTime in alarmsAtTime) {

            if (!handle.isValid()) throw InvalidatedStateException()

            // Show alarm

            // set group state to idle if last alarm in group
            if (alarmAtTime.isLastInGroup)
                stateRepository.updateState(
                    StateData.Idle(
                        alarmAtTime.groupUuid,
                    )
                )
        }
    }

    /**
     * @param handle to check if valid
     * @param dateTime to schedule from
     * @throws InvalidatedStateException when handle is found to be out of date
     */
    private suspend fun scheduleWake(handle: Lock.Handle, dateTime: LocalDateTime) {
        if (!handle.isValid()) throw InvalidatedStateException()
        // Repeated call from race condition should not be an issue
        wakeScheduler.tryScheduleWake(fromDateTime = dateTime)
    }
}