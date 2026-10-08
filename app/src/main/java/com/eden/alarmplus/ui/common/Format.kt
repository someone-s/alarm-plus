package com.eden.alarmplus.ui.common

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Immutable
data class Format(
    val timeFormatter: DateTimeFormatter
)

val LocalFormat = compositionLocalOf { Format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) }