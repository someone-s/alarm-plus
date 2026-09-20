package com.eden.alarmplus.main.logic

import android.content.Context
import java.time.LocalDateTime

class StateProcessor(private val context: Context) {


    /**
     * Use when state is not guaranteed (i.e. time zone changes or from restart)
     * @param dateTime the lower bound exclusive to process
     */
    suspend fun initializeState(dateTime: LocalDateTime) {

        WakeScheduler(context).scheduleWake(fromDateTime = dateTime)
    }

    /**
     * Assume either there were previous [initializeState] or [iterateState] ran
     * Assume time zone remains unchanged, within time zone jump is acceptable (i.e. summer to winter)
     * @param fromDateTime the lower bound exclusive to process
     * @param toDateTime the upper bound inclusive to process
     */
    suspend fun iterateState(fromDateTime: LocalDateTime, toDateTime: LocalDateTime) {

        WakeScheduler(context).scheduleWake(fromDateTime = toDateTime)
    }
}