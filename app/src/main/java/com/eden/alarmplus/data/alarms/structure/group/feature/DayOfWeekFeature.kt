package com.eden.alarmplus.data.alarms.structure.group.feature

import androidx.compose.runtime.Stable
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
): RestrictionFeature {
}