package com.eden.alarmplus.data.datetime.repository

import kotlinx.coroutines.flow.Flow
import java.time.format.DateTimeFormatter

interface TimeFormatRepository {
    data class Values(
        val is24Formatted: Boolean,
        val timeFormatter: DateTimeFormatter,
    )

    fun currentTimeFormat(): Flow<Values>
}