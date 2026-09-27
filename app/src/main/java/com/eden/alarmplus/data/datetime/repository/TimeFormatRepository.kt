package com.eden.alarmplus.data.datetime.repository

import kotlinx.coroutines.flow.Flow
import java.time.format.DateTimeFormatter

interface TimeFormatRepository {
    fun is24Formatted(): Flow<Boolean>

    fun currentTimeFormat(): Flow<DateTimeFormatter>
}