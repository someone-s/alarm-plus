package com.eden.alarmplus.data.repository

import com.eden.alarmplus.data.database.AlarmEntity
import com.eden.alarmplus.data.database.GroupAlarmsDao
import com.eden.alarmplus.data.database.GroupEntity
import com.eden.alarmplus.data.database.UuidLongs
import com.eden.alarmplus.structure.alarm.AlarmData
import com.eden.alarmplus.structure.alarm.AlarmFeature
import com.eden.alarmplus.structure.alarm.AlarmInfo
import com.eden.alarmplus.structure.formatter
import com.eden.alarmplus.structure.group.GroupData
import com.eden.alarmplus.structure.group.GroupInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import kotlin.collections.map
import kotlin.uuid.Uuid

class OfflineGroupAlarmsRepository(private val groupAlarmsDao: GroupAlarmsDao): GroupAlarmsRepository {
    override suspend fun insert(group: GroupData) {
        groupAlarmsDao.insert(group.info.toGroupEntity(), group.alarms.map { it.toAlarmEntity(group.info.uuid) })
    }

    override suspend fun toggle(groupUuid: Uuid, active: Boolean) {
        val longs = groupUuid.toUuidLongs()
        groupAlarmsDao.toggle(longs.upperUuid, longs.lowerUuid, active)
    }

    override suspend fun delete(groupUuid: Uuid) {
        groupAlarmsDao.delete(GroupEntity(
            uuid = groupUuid.toUuidLongs(),
            name = null,
            active = false,
        ))
    }

    override suspend fun insertOrReplace(group: GroupData) {
        delete(group.info.uuid)
        insert(group)
    }

    override suspend fun getNextAlarmTimeAfter(time: LocalTime): LocalTime {
        val fromTimestamp = time.hour * 60 + time.minute
        val nextTimestamp = groupAlarmsDao.getTimestampAfter(fromTimestamp)
        return LocalTime.of(nextTimestamp / 60, nextTimestamp % 60)
    }

    override fun getAllGroupAlarmsStream(): Flow<List<GroupData>> {
        return groupAlarmsDao.getAll()
            .map { groupsWithAlarms ->
                groupsWithAlarms.map { groupWithAlarms ->
                    GroupData(
                        info = groupWithAlarms.group.toGroupInfo(),
                        alarms = groupWithAlarms.alarms.map { alarm -> alarm.toAlarmData() },
                    )
                }
            }
    }

    private fun UuidLongs.toUuid() = Uuid.fromLongs(this.upperUuid, this.lowerUuid)

    private fun Uuid.toUuidLongs(): UuidLongs {
        val upperUuid: Long
        val lowerUuid: Long
        this.toLongs { mostSignificantBits, leastSignificantBits ->
            upperUuid = mostSignificantBits
            lowerUuid = leastSignificantBits
        }

        return UuidLongs(
            upperUuid = upperUuid,
            lowerUuid = lowerUuid,
        )
    }

    private fun GroupInfo.toGroupEntity() = GroupEntity(
        uuid = this.uuid.toUuidLongs(),
        name = this.name,
        active = this.active,
    )

    private fun GroupEntity.toGroupInfo() = GroupInfo(
        uuid = this.uuid.toUuid(),
        name = this.name,
        active = this.active,
    )

    private fun AlarmData.toAlarmEntity(group: Uuid): AlarmEntity {

        val timestamp = this.info.time.let { it.hour * 60 + it.minute }

        val features = formatter.encodeToString(this.features)

        return AlarmEntity(
            alarmUuid = this.info.uuid.toUuidLongs(),
            groupUuid = group.toUuidLongs(),
            timestamp = timestamp,
            features = features,
        )
    }

    private fun AlarmEntity.toAlarmData(): AlarmData {
        val time = LocalTime.of(this.timestamp / 60, this.timestamp % 60)

        val features: List<AlarmFeature> = formatter.decodeFromString(this.features)

        return AlarmData(
            info = AlarmInfo(
                uuid = this.alarmUuid.toUuid(),
                time = time,
            ),
            features = features,
        )
    }
}
