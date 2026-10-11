package com.eden.alarmplus.ui.common

import com.eden.alarmplus.data.datetime.repository.TimeFormatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val mockFormatViewModel = FormatViewModel(
    initialFormat = CurrentFormatUiState(false, DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
    timeFormatRepository = object : TimeFormatRepository {
        override fun currentTimeFormat(): Flow<TimeFormatRepository.Values> {
            return flowOf()
        }
    },
)