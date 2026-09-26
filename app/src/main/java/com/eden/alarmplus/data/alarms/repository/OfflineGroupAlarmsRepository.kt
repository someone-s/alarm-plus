package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.database.GroupsWithAlarmsDao
import com.eden.alarmplus.data.alarms.structure.group.GroupData
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
            groupsWithAlarmsDao.insertOrReplace(group.info.toGroupEntity(), group.alarms.map { it.toAlarmEntity(group.info.uuid) })
            super.insertOrReplace(group)
        }
    }

    override suspend fun toggle(groupUuid: Uuid, active: Boolean) {
        withContext(NonCancellable) {
            val longs = groupUuid.toUuidLongs()
            groupsWithAlarmsDao.toggle(longs.upperUuid, longs.lowerUuid, active)
            super.toggle(groupUuid, active)
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

    override fun getAllGroupsStream(): Flow<List<GroupData>> {
        return groupsWithAlarmsDao.getAll()
            .map { groupsWithAlarms ->
                groupsWithAlarms
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
    }
}
