package com.eden.alarmplus.data.structure.alarm.feature

import com.eden.alarmplus.data.structure.alarm.AlarmFeature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.eden.alarmplus.data.structure.audio.AudioData

@Serializable
@SerialName("audio-feature")
data class AudioFeature(
    @SerialName("data")
    val data: AudioData?
): AlarmFeature {
}