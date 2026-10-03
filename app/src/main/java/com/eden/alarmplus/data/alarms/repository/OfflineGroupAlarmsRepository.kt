package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.database.GroupsWithAlarmsDao
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.state.StateData
import com.eden.alarmplus.data.common.repository.toUuidLongs
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalTime
import kotlin.collections.map
import kotlin.uuid.Uuid

class OfflineGroupAlarmsRepository(
    private val groupsWithAlarmsDao: GroupsWithAlarmsDao,
    private val notificationListener: NotificationListener,
): GroupAlarmsRepository {

    abstract class NotificationListener {
        abstract suspend fun onNotification(notification: GroupAlarmsRepository.ChangeNotification)
    }

    override suspend fun insertOrReplace(group: GroupData) {
        withContext(NonCancellable) {
            groupsWithAlarmsDao.insertOrReplace(
                group = group.info.toGroupEntity(),
                alarms = group.alarms.mapIndexed { index, alarm ->
                    alarm.toAlarmEntity(index, group.info.uuid)
                })
            super.insertOrReplace(group)
        }
    }

    override suspend fun toggle(groupUuid: Uuid, enabled: Boolean) {
        withContext(NonCancellable) {
            val longs = groupUuid.toUuidLongs()
            groupsWithAlarmsDao.toggle(longs.upperUuid, longs.lowerUuid, enabled)
            super.toggle(groupUuid, enabled)
        }
    }

    override suspend fun delete(groupUuid: Uuid) {
        withContext(NonCancellable) {
            val longs = groupUuid.toUuidLongs()
            groupsWithAlarmsDao.delete(longs.upperUuid, longs.lowerUuid)
            super.delete(groupUuid)
        }
    }

    override suspend fun notify(notification: GroupAlarmsRepository.ChangeNotification) {
        notificationListener.onNotification(notification)
    }

    override fun getAllGroupsStream(): Flow<List<Pair<GroupData, StateData>>> {
        return groupsWithAlarmsDao.getAll()
            .map { groupsWithAlarms ->
                groupsWithAlarms
                    .map { groupWithAlarms ->
                        Pair(
                            GroupData(
                                info = groupWithAlarms.group.toGroupInfo(),
                                alarms = groupWithAlarms.unorderedAlarms
                                    .sortedBy { alarm -> alarm.sortValue }
                                    .map { alarm -> alarm.toAlarmData() },
                            ),
                            groupWithAlarms.state.toStateData()
                        )
                    }
                    .sortedBy { pair ->
                        if (pair.first.alarms.isEmpty()) LocalTime.of(0, 0)
                        else pair.first.alarms.minOf { a -> a.info.time }
                    }
            }
    }
}
