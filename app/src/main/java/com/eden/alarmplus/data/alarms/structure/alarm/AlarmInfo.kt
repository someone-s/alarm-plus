package com.eden.alarmplus.data.alarms.structure.alarm

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.LocalTime
import kotlin.uuid.Uuid

@Immutable
@Serializable
@Parcelize
data class AlarmInfo(
    val uuid: Uuid = Uuid.random(),
    @Serializable(LocalTimeSerializer::class)
    val time: LocalTime,
) : Parcelable {
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