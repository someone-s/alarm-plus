package com.eden.alarmplus.ui.common

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eden.alarmplus.AlarmPlusApplication
import com.eden.alarmplus.data.datetime.repository.TimeFormatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.format.DateTimeFormatter

object MockFormatViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val defaultFormatter = DateTimeFormatter.ofPattern("HH:mm")
            FormatViewModel(
                CurrentFormatUiState(defaultFormatter),
                object : TimeFormatRepository {
                    override fun is24Formatted(): Flow<Boolean> {
                        return flowOf(true)
                    }

                    override fun currentTimeFormat(): Flow<DateTimeFormatter> {
                        return flowOf(defaultFormatter)
                    }
                },
            )
        }
    }
}