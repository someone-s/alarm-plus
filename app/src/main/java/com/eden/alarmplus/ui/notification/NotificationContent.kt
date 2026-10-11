package com.eden.alarmplus.ui.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eden.alarmplus.ui.common.Format
import com.eden.alarmplus.ui.common.FormatViewModel
import com.eden.alarmplus.ui.common.FormatViewModelProvider
import com.eden.alarmplus.ui.common.LocalFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun NotificationContent(
    title: String?,
    time: LocalTime?,
    onSnooze: (() -> Unit)?,
    onDismiss: (() -> Unit)?,
    formatViewModel: FormatViewModel = viewModel(factory = FormatViewModelProvider.Factory),
) {
    val currentFormatUiState by formatViewModel.currentFormatUiState.collectAsState()
    val format = Format(
        timeFormatter = currentFormatUiState.timeFormatter,
        largeHourFormatter = DateTimeFormatter.ofPattern(
            if (currentFormatUiState.is24Formatted) "HH"
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
