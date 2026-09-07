package com.eden.alarmplus.data

import java.time.LocalTime

data class AlarmData(
    val time: LocalTime,
    val status: AlarmStatus,
    val features: List<AlarmFeature>,
)

val emptyAlarmData = AlarmData(
    time = LocalTime.of(0, 0),
    status = AlarmStatus.DISABLED,
    features = emptyList(),
)