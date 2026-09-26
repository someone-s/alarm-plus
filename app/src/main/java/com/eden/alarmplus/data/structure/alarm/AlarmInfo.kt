package com.eden.alarmplus.data.structure.alarm

import java.time.LocalTime
import kotlin.uuid.Uuid

data class AlarmInfo(
    val uuid: Uuid = Uuid.random(),
    val time: LocalTime,
) {
    fun shallowCopy(time: LocalTime) = AlarmInfo(
        uuid = this.uuid,
        time = time,
    )
}

val emptyAlarmInfo = AlarmInfo(
    time = LocalTime.of(0, 0),
)

fun getUniqueAlarmInfo() = AlarmInfo(
    time = LocalTime.of(0, 0),
)