package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.database.StateDao
import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.state.StateData
import com.eden.alarmplus.data.common.repository.toUuid
import com.eden.alarmplus.data.common.repository.toUuidLongs
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import kotlin.uuid.Uuid

class OfflineStateRepository(
    private val stateDao: StateDao
): StateRepository {

    private fun DayOfWeek.toFlags() = when (this) {
        DayOfWeek.MONDAY -> DayOfWeekFlags.MONDAY.mask
        DayOfWeek.TUESDAY -> DayOfWeekFlags.TUESDAY.mask
        DayOfWeek.WEDNESDAY -> DayOfWeekFlags.WEDNESDAY.mask
        DayOfWeek.THURSDAY -> DayOfWeekFlags.THURSDAY.mask
        DayOfWeek.FRIDAY -> DayOfWeekFlags.FRIDAY.mask
        DayOfWeek.SATURDAY -> DayOfWeekFlags.SATURDAY.mask
        DayOfWeek.SUNDAY -> DayOfWeekFlags.SUNDAY.mask
    }

    override suspend fun findNextTimeAfter(day: DayOfWeek, time: LocalTime, window: Duration): LocalTime? {
        val fromTimestamp = time.hour * 60 + time.minute
        val nextTimestamp = stateDao.findNextTimestampAfter(
            day = day.toFlags(),
            timestamp = fromTimestamp,
            window = window.toMinutes().toInt()
        ) ?: return null
        return LocalTime.of(nextTimestamp / 60, nextTimestamp % 60)
    }

    override suspend fun getAllGroups(): List<GroupData> {
        return stateDao.getAllGroups()
            .map { groupWithAll ->
                GroupData(
                    info = groupWithAll.group.toGroupInfo(),
                    alarms = groupWithAll.unorderedAlarms
                        .sortedBy { alarm -> alarm.sortValue }
                        .map { alarm -> alarm.toAlarmData() },
                    dayOfWeekFlags = groupWithAll.dayOfWeekEntity?.toDayOfWeekFlags() ?: DayOfWeekFlags.ALL,
                )
            }
            .sortedBy { groupData ->
                if (groupData.alarms.isEmpty()) LocalTime.of(0, 0)
                else groupData.alarms.minOf { a -> a.info.time }
            }
    }

    override suspend fun getAlarmsAtTime(day: DayOfWeek, time: LocalTime, window: Duration): List<StateRepository.AlarmsAtTime> {
        val timestamp = time.hour * 60 + time.minute
        return stateDao.getAlarmsWithGroupsAtTimestamp(
            day = day.toFlags(),
            timestamp = timestamp,
            window = window.toMinutes().toInt()
        )
            .map { alarmAtTimestamp ->
                StateRepository.AlarmsAtTime(
                    alarm = alarmAtTimestamp.alarm.toAlarmData(),
                    group = alarmAtTimestamp.group.toGroupInfo(),
                    isLastInGroup = alarmAtTimestamp.isLastInGroup,
                    inDismissableWindow = alarmAtTimestamp.inDismissableWindow
                )
            }
    }

    override suspend fun getPrimedAtTime(day: DayOfWeek, time: LocalTime, window: Duration): List<Uuid> {
        val timestamp = time.hour * 60 + time.minute
        return stateDao.getPrimedAtTimestamp(
            day = day.toFlags(),
            timestamp = timestamp,
            window = window.toMinutes().toInt()
        )
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