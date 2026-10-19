package com.eden.alarmplus.data.alarms.structure.group.feature

import androidx.compose.runtime.Stable
import com.eden.alarmplus.data.alarms.structure.LocalDateSerializer
import com.eden.alarmplus.data.alarms.structure.group.GroupFeature
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Stable
@Serializable
@SerialName("schedule-end-feature")
@Parcelize
data class ScheduleEndFeature(
    @Serializable(LocalDateSerializer::class)
    val date: LocalDate,
): GroupFeature