package com.eden.alarmplus.data

import java.time.LocalTime
import kotlin.uuid.Uuid

data class AlarmData(
    val uuid: Uuid = Uuid.random(),
    val time: LocalTime,
    val active: Boolean,
    val features: List<AlarmFeature>,
) {
    fun shallowCopy(time: LocalTime) = AlarmData(
        uuid = this.uuid,
        time = time,
        active = this.active,
        features = this.features,
    )

    fun shallowCopy(active: Boolean) = AlarmData(
        uuid = this.uuid,
        time = this.time,
        active = active,
        features = this.features,
    )
}

val emptyAlarmData = AlarmData(
    time = LocalTime.of(0, 0),
    active = true,
    features = emptyList(),
)