package com.eden.alarmplus.data.alarm

import java.time.LocalTime

data class AlarmData(
    val info: AlarmInfo,
    val features: List<AlarmFeature>,
) {
    fun shallowCopy(time: LocalTime) = AlarmData(
        info = this.info.shallowCopy(time),
        features = this.features,
    )

    fun shallowCopyAddFeature(feature: AlarmFeature) = AlarmData(
        info = this.info,
        features = this.features + feature
    )

    inline fun <reified T: AlarmFeature> shallowCopyRemoveFeature() = AlarmData(
        info = this.info,
        features = this.features.filter { it !is T }
    )

    inline fun <reified T: AlarmFeature> getFeature(): T? {
        return features.firstOrNull { it is T } as? T
    }
}

val emptyAlarmData = AlarmData(
    info = emptyAlarmInfo,
    features = emptyList(),
)