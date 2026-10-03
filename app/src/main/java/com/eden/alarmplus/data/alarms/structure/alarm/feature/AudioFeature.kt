package com.eden.alarmplus.data.alarms.structure.alarm.feature

import androidx.compose.runtime.Stable
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmFeature
import com.eden.alarmplus.data.alarms.structure.audio.AudioData
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
@SerialName("audio-feature")
@Parcelize
data class AudioFeature(
    @SerialName("data")
    val data: AudioData?
): AlarmFeature {
}