package com.eden.alarmplus.ui.common.format

import android.text.format.DateFormat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eden.alarmplus.AlarmPlusApplication
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

object FormatViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = mainApplication()
            val container = application.container
            FormatViewModel(
                CurrentFormatUiState(
                    is24Formatted = DateFormat.is24HourFormat(application),
                    timeFormatter = DateTimeFormatter.ofPattern(
                        (DateFormat.getTimeFormat(application) as SimpleDateFormat).toPattern()
                    ),
                ),
                container.timeFormatRepository,
            )
        }
    }
}

private fun CreationExtras.mainApplication(): AlarmPlusApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AlarmPlusApplication