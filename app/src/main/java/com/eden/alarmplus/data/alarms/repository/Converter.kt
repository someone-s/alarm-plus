package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.database.AlarmEntity
import com.eden.alarmplus.data.alarms.database.GroupEntity
import com.eden.alarmplus.data.alarms.database.StateEntity
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmFeature
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmInfo
import com.eden.alarmplus.data.alarms.structure.formatter
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.state.StateData
import com.eden.alarmplus.data.common.repository.toUuid
import com.eden.alarmplus.data.common.repository.toUuidLongs
import java.time.LocalTime
import kotlin.uuid.Uuid

fun GroupInfo.toGroupEntity() = GroupEntity(
    uuid = this.uuid.toUuidLongs(),
    name = this.name,
    active = this.active,
)

fun GroupEntity.toGroupInfo() = GroupInfo(
    uuid = this.uuid.toUuid(),
    name = this.name,
    active = this.active,
)

fun AlarmData.toAlarmEntity(group: Uuid): AlarmEntity {

    val timestamp = this.info.time.let { it.hour * 60 + it.minute }

    val features = formatter.encodeToString(this.features)

    return AlarmEntity(
        alarmUuid = this.info.uuid.toUuidLongs(),
        groupUuid = group.toUuidLongs(),
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

fun StateData.toStateEntity() = StateEntity(
    groupUuid = this.groupUuid.toUuidLongs(),
    value = when(this) {
        is StateData.Active -> StateEntity.ACTIVE_VALUE
        is StateData.Dismissed -> StateEntity.DISMISSED_VALUE
        is StateData.Idle -> StateEntity.IDLE_VALUE
        is StateData.Undefined -> StateEntity.UNDEFINED_VALUE
    }
)

fun StateEntity.toStateData() = when(this.value) {
    StateEntity.ACTIVE_VALUE -> StateData.Active(this.groupUuid.toUuid())
    StateEntity.DISMISSED_VALUE -> StateData.Dismissed(this.groupUuid.toUuid())
    StateEntity.IDLE_VALUE -> StateData.Idle(this.groupUuid.toUuid())
    StateEntity.UNDEFINED_VALUE -> StateData.Undefined(this.groupUuid.toUuid())
    else -> throw IllegalArgumentException("StateEntity.value is ${this.value}, not recognized.")
}