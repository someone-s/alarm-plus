package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.database.AlarmEntity
import com.eden.alarmplus.data.alarms.database.GroupEntity
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmFeature
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmInfo
import com.eden.alarmplus.data.alarms.structure.formatter
import com.eden.alarmplus.data.alarms.structure.group.GroupFeature
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.state.StateType
import com.eden.alarmplus.data.common.repository.toUuid
import com.eden.alarmplus.data.common.repository.toUuidLongs
import java.time.LocalTime
import kotlin.uuid.Uuid

fun GroupInfo.toGroupEntity(): GroupEntity {

    val features = formatter.encodeToString(this.features)

    return GroupEntity(
        uuid = this.uuid.toUuidLongs(),
        name = this.name,
        features = features,
        state = this.state.toInt(),
    )
}

fun GroupEntity.toGroupInfo(): GroupInfo {
    val features: List<GroupFeature> = formatter.decodeFromString(this.features)

    return GroupInfo(
        uuid = this.uuid.toUuid(),
        name = this.name,
        features = features,
        state = this.state.toStateType(),
    )
}

fun AlarmData.toAlarmEntity(sortValue: Int, group: Uuid): AlarmEntity {

    val timestamp = this.info.time.let { it.hour * 60 + it.minute }

    val features = formatter.encodeToString(this.features)

    return AlarmEntity(
        alarmUuid = this.info.uuid.toUuidLongs(),
        groupUuid = group.toUuidLongs(),
        sortValue = sortValue,
        timestamp = timestamp,
        features = features,
    )
}

fun AlarmEntity.toAlarmData(): AlarmData {
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

fun StateType.toInt() = when(this) {
    StateType.DISABLED -> GroupEntity.DISABLED_VALUE
    StateType.ENABLED_PENDING -> GroupEntity.ENABLED_PENDING_VALUE
    StateType.ENABLED_PRIMED -> GroupEntity.ENABLED_PRIMED_VALUE
    StateType.ENABLED_ACTIVE -> GroupEntity.ENABLED_ACTIVE_VALUE
    StateType.ENABLED_DISMISSED -> GroupEntity.ENABLED_DISMISSED_VALUE
    StateType.ENABLED_IDLE -> GroupEntity.ENABLED_IDLE_VALUE
    StateType.ENABLED_UNDEFINED -> GroupEntity.ENABLED_UNDEFINED_VALUE
}

fun Int.toStateType() = when(this) {
    GroupEntity.DISABLED_VALUE -> StateType.DISABLED
    GroupEntity.ENABLED_PENDING_VALUE -> StateType.ENABLED_PENDING
    GroupEntity.ENABLED_PRIMED_VALUE -> StateType.ENABLED_PRIMED
    GroupEntity.ENABLED_ACTIVE_VALUE -> StateType.ENABLED_ACTIVE
    GroupEntity.ENABLED_DISMISSED_VALUE -> StateType.ENABLED_DISMISSED
    GroupEntity.ENABLED_IDLE_VALUE -> StateType.ENABLED_IDLE
    GroupEntity.ENABLED_UNDEFINED_VALUE -> StateType.ENABLED_UNDEFINED
    else -> throw IllegalArgumentException("StateEntity.value is $this, not recognized.")
}