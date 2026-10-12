package com.eden.alarmplus.ui.common.format

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eden.alarmplus.data.datetime.repository.TimeFormatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.format.DateTimeFormatter

class FormatViewModel(initialFormat: CurrentFormatUiState, timeFormatRepository: TimeFormatRepository): ViewModel() {

    val currentFormatUiState: StateFlow<CurrentFormatUiState> =
        timeFormatRepository.currentTimeFormat()
            .map {
                CurrentFormatUiState(it.is24Formatted, it.timeFormatter)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS, 600_000L),
                initialValue = initialFormat,
            )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

@Immutable
data class CurrentFormatUiState(
    val is24Formatted: Boolean,
    val timeFormatter: DateTimeFormatter
)