package com.eden.alarmplus.ui.common

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eden.alarmplus.data.datetime.repository.TimeFormatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class FormatViewModel(context: Context, timeFormatRepository: TimeFormatRepository): ViewModel() {

    val currentFormatUiState: StateFlow<CurrentTimeFormatUiState> =
        timeFormatRepository.currentTimeFormat()
            .map {
                CurrentTimeFormatUiState(it)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS, 600_000L),
                initialValue = CurrentTimeFormatUiState(
                    DateTimeFormatter.ofPattern(
                        (DateFormat.getTimeFormat(context) as SimpleDateFormat).toPattern()
                    )
                ),
            )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

@Immutable
data class CurrentTimeFormatUiState(
    val timeFormatter: DateTimeFormatter
)