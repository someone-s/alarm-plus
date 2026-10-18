package com.eden.alarmplus.data.alarms.structure.group.feature

import androidx.compose.runtime.Stable
import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags
import com.eden.alarmplus.data.alarms.structure.group.GroupFeature
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
@SerialName("day-of-week-feature")
@Parcelize
data class DayOfWeekFeature(
    @SerialName("flags")
    val flags: DayOfWeekFlags
): GroupFeature {
}