package com.eden.alarmplus.data.repository

import com.eden.alarmplus.structure.alarm.AlarmData
import com.eden.alarmplus.structure.group.GroupData
import com.eden.alarmplus.structure.state.StateData
import java.time.LocalTime
import kotlin.uuid.Uuid

interface StateRepository {

    data class AlarmsAtTime(
        val alarm: AlarmData,
        val groupUuid: Uuid,
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