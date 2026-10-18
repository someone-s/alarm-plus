package com.eden.alarmplus.ui.common.format

import android.text.format.DateFormat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eden.alarmplus.AlarmPlusApplication
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

object DateFormatViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = mainApplication()
            val container = application.container
            DateFormatViewModel(
                CurrentDateFormatUiState(
                    dateFormatter = DateTimeFormatter.ofPattern(
                        (DateFormat.getDateFormat(application) as SimpleDateFormat).toPattern()
                    ),
                ),
                container.dateFormatRepository,
            )
        }
    }
}

private fun CreationExtras.mainApplication(): AlarmPlusApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AlarmPlusApplication