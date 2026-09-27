package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.state.StateData
import java.time.LocalTime
import kotlin.uuid.Uuid

interface StateRepository {

    data class AlarmsAtTime(
        val alarm: AlarmData,
        val group: GroupInfo,
        val isLastInGroup: Boolean,
    )

    /**
     * Wrap around find next alarm time
     * @param time time exclusive to look for next nearest alarm time
     * @return next alarm time, null if no alarms
     */
    suspend fun findNextActiveAlarmTimeAfter(time: LocalTime): LocalTime?

    suspend fun getAllGroups(): List<GroupData>

    suspend fun getAlarmsAtTime(time: LocalTime): List<AlarmsAtTime>

    suspend fun getState(groupUuid: Uuid): StateData?

    suspend fun updateState(state: StateData)
}