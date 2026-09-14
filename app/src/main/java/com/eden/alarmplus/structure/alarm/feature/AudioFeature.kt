package com.eden.alarmplus.structure.alarm.feature

import com.eden.alarmplus.structure.alarm.AlarmFeature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.eden.alarmplus.structure.audio.AudioData

@Serializable
@SerialName("audio-feature")
data class AudioFeature(
    val data: AudioData?
): AlarmFeature {
}