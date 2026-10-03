package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.database.StateDao
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.state.StateData
import com.eden.alarmplus.data.common.repository.toUuid
import com.eden.alarmplus.data.common.repository.toUuidLongs
import java.time.Duration
import java.time.LocalTime
import kotlin.uuid.Uuid

class OfflineStateRepository(
    private val stateDao: StateDao
): StateRepository {

    override suspend fun findNextTimeAfter(time: LocalTime, window: Duration): LocalTime? {
        val fromTimestamp = time.hour * 60 + time.minute
        val nextTimestamp = stateDao.findNextTimestampAfter(fromTimestamp, window.toMinutes().toInt()) ?: return null
        return LocalTime.of(nextTimestamp / 60, nextTimestamp % 60)
    }

    override suspend fun getAllGroups(): List<GroupData> {
        return stateDao.getAllGroupsWithAlarms()
            .map { groupWithAlarms ->
                GroupData(
                    info = groupWithAlarms.group.toGroupInfo(),
                    alarms = groupWithAlarms.unorderedAlarms
                        .sortedBy { alarm -> alarm.sortValue }
                        .map { alarm -> alarm.toAlarmData() },
                )
            }
            .sortedBy { groupData ->
                if (groupData.alarms.isEmpty()) LocalTime.of(0, 0)
                else groupData.alarms.minOf { a -> a.info.time }
            }
    }

    override suspend fun getAlarmsAtTime(time: LocalTime, window: Duration): List<StateRepository.AlarmsAtTime> {
        val timestamp = time.hour * 60 + time.minute
        return stateDao.getAlarmsWithGroupsAtTimestamp(timestamp, window.toMinutes().toInt())
            .map { alarmAtTimestamp ->
                StateRepository.AlarmsAtTime(
                    alarm = alarmAtTimestamp.alarm.toAlarmData(),
                    group = alarmAtTimestamp.group.toGroupInfo(),
                    isLastInGroup = alarmAtTimestamp.isLastInGroup,
                    inDismissableWindow = alarmAtTimestamp.inDismissableWindow
                )
            }
    }

    override suspend fun getPrimedAtTime(time: LocalTime, window: Duration): List<Uuid> {
        val timestamp = time.hour * 60 + time.minute
        return stateDao.getPrimedAtTimestamp(timestamp, window.toMinutes().toInt())
            .map { it.groupUuid.toUuid() }
    }

    override suspend fun getState(groupUuid: Uuid): StateData? {
        val longs = groupUuid.toUuidLongs()
        return stateDao.getState(longs.upperUuid, longs.lowerUuid)?.toStateData()
    }

    override suspend fun updateState(state: StateData) {
        stateDao.updateState(state.toStateEntity())
    }
}