package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.database.StateDao
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.GroupFeature
import com.eden.alarmplus.data.alarms.structure.state.StateType
import com.eden.alarmplus.data.common.repository.toUuidLongs
import java.time.LocalTime
import kotlin.uuid.Uuid

class OfflineStateRepository(
    private val stateDao: StateDao
): StateRepository {

    override suspend fun getAllGroups(): List<GroupData> {
        return stateDao.getAllGroups()
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

    override suspend fun getState(groupUuid: Uuid): StateType? {
        val longs = groupUuid.toUuidLongs()
        return stateDao.getState(longs.upperUuid, longs.lowerUuid)?.toStateType()
    }

    override suspend fun setGroupFeatures(groupUuid: Uuid, features: List<GroupFeature>) {
        val longs = groupUuid.toUuidLongs()
        stateDao.setGroupFeatures(longs.upperUuid, longs.lowerUuid, features.toGroupFeaturesString())
    }

    override suspend fun updateState(groupUuid: Uuid, state: StateType) {
        val longs = groupUuid.toUuidLongs()
        stateDao.updateState(longs.upperUuid, longs.lowerUuid, state.toInt())
    }
}