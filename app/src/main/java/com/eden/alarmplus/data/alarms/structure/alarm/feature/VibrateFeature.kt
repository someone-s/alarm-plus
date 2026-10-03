package com.eden.alarmplus.data.alarms.structure.alarm.feature

import androidx.compose.runtime.Stable
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmFeature
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
@SerialName("vibrate-feature")
@Parcelize
class VibrateFeature: AlarmFeature {
}