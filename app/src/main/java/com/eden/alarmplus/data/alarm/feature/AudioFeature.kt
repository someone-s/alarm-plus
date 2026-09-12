package com.eden.alarmplus.data.alarm.feature

import com.eden.alarmplus.data.alarm.AlarmFeature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.eden.alarmplus.data.audio.AudioData

@Serializable
@SerialName("audio-feature")
data class AudioFeature(
    val data: AudioData
): AlarmFeature {
}