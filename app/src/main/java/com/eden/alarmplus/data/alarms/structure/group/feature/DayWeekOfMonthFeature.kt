package com.eden.alarmplus.data.alarms.structure.group.feature

import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This is used to represent the limit day of week to the nth of the month,
 * i.e. First Monday of the month or Fifth Monday of the month
 */
@Stable
@Serializable
@SerialName("day-week-of-month-feature")
@Parcelize
data class DayWeekOfMonthFeature(
    @SerialName("flags")
    val flags: DayWeekOfMonthFlags
): RestrictionFeature {
}