package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.database.StateDao
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.state.StateData
import com.eden.alarmplus.data.common.repository.toUuidLongs
import java.time.LocalTime
import kotlin.uuid.Uuid

class OfflineStateRepository(
    private val stateDao: StateDao
): StateRepository {

    override suspend fun getAllGroups(): List<GroupData> {
        return stateDao.getAllGroups()
            .map { groupWithAll ->
                GroupData(
                    info = groupWithAll.group.toGroupInfo(),
                    alarms = groupWithAll.unorderedAlarms
                        .sortedBy { alarm -> alarm.sortValue }
                        .map { alarm -> alarm.toAlarmData() },
                )
            }
            .sortedBy { groupData ->
                if (groupData.alarms.isEmpty()) LocalTime.of(0, 0)
                else groupData.alarms.minOf { a -> a.info.time }
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