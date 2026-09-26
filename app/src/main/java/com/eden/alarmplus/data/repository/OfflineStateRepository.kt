package com.eden.alarmplus.data.repository

import com.eden.alarmplus.data.database.StateDao
import com.eden.alarmplus.data.structure.group.GroupData
import com.eden.alarmplus.data.structure.state.StateData
import java.time.LocalTime
import kotlin.uuid.Uuid

class OfflineStateRepository(private val stateDao: StateDao): StateRepository {

    override suspend fun findNextActiveAlarmTimeAfter(time: LocalTime): LocalTime? {
        val fromTimestamp = time.hour * 60 + time.minute
        val nextTimestamp = stateDao.findNextActiveTimestampAfter(fromTimestamp) ?: return null
        return LocalTime.of(nextTimestamp / 60, nextTimestamp % 60)
    }

    override suspend fun getAllGroups(): List<GroupData> {
        return stateDao.getAllGroupsWithAlarms()
            .map { groupWithAlarms ->
                GroupData(
                    info = groupWithAlarms.group.toGroupInfo(),
                    alarms = groupWithAlarms.alarms.map { alarm -> alarm.toAlarmData() },
                )
            }
            .sortedBy { groupData ->
                if (groupData.alarms.isEmpty()) LocalTime.of(0, 0)
                else groupData.alarms.minOf { a -> a.info.time }
            }
    }

    override suspend fun getAlarmsAtTime(time: LocalTime): List<StateRepository.AlarmsAtTime> {
        val timestamp = time.hour * 60 + time.minute
        return stateDao.getAlarmsWithGroupsAtTimestamp(timestamp)
            .map { alarmAtTimestamp ->
                StateRepository.AlarmsAtTime(
                    alarm = alarmAtTimestamp.alarm.toAlarmData(),
                    groupUuid = alarmAtTimestamp.alarm.groupUuid.toUuid(),
                    isLastInGroup = alarmAtTimestamp.isLastInGroup,
                )
            }
    }

    override suspend fun getState(groupUuid: Uuid): StateData? {
        val longs = groupUuid.toUuidLongs()
        return stateDao.getState(longs.upperUuid, longs.lowerUuid)?.toStateData()
    }

    override suspend fun updateState(state: StateData) {
        stateDao.updateState(state.toStateEntity())
    }
}