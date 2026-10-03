package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.state.StateData
import java.time.Duration
import java.time.LocalTime
import kotlin.uuid.Uuid

interface StateRepository {

    data class AlarmsAtTime(
        val alarm: AlarmData,
        val group: GroupInfo,
        val isLastInGroup: Boolean,
        val inDismissableWindow: Boolean,
    )

    /**
     * Wrap around find next alarm or dismissable window start time
     * @param time time exclusive to look for next nearest alarm or dismissable window start time
     * @param window time ahead of first alarm where group can be dismissed
     * @return next alarm or dismissable window start time, null if no alarms
     */
    suspend fun findNextTimeAfter(time: LocalTime, window: Duration): LocalTime?

    suspend fun getAllGroups(): List<GroupData>

    suspend fun getAlarmsAtTime(time: LocalTime, window: Duration): List<AlarmsAtTime>

    /**
     * @return group Uuid
     */
    suspend fun getPrimedAtTime(time: LocalTime, window: Duration): List<Uuid>

    suspend fun getState(groupUuid: Uuid): StateData?

    suspend fun updateState(state: StateData)
}