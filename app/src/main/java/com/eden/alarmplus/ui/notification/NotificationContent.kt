package com.eden.alarmplus.ui.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eden.alarmplus.ui.common.format.Format
import com.eden.alarmplus.ui.common.format.TimeFormatViewModel
import com.eden.alarmplus.ui.common.format.TimeFormatViewModelProvider
import com.eden.alarmplus.ui.common.format.LocalFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun NotificationContent(
    title: String?,
    time: LocalTime?,
    onSnooze: (() -> Unit)?,
    onDismiss: (() -> Unit)?,
    timeFormatViewModel: TimeFormatViewModel = viewModel(factory = TimeFormatViewModelProvider.Factory),
) {
    val currentTimeFormatUiState by timeFormatViewModel.currentTimeFormatUiState.collectAsState()
    val format = Format(
        timeFormatter = currentTimeFormatUiState.timeFormatter,
        largeHourFormatter = DateTimeFormatter.ofPattern(
            if (currentTimeFormatUiState.is24Formatted) "HH"
            else "hh"
        ),
    )

    CompositionLocalProvider(LocalFormat provides format) {
        NotificationScreen(
            title = title,
            time = time,
            onSnooze = onSnooze,
            onDismiss = onDismiss,
        )
    }
}
