package com.eden.alarmplus.ui.common.format

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Immutable
data class Format(
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT),
    val largeHourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH"),
    val largeMinuteFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("mm"),
)

val LocalFormat = compositionLocalOf { Format() }