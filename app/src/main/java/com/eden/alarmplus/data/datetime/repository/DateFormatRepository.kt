package com.eden.alarmplus.data.datetime.repository

import kotlinx.coroutines.flow.Flow
import java.time.format.DateTimeFormatter

interface DateFormatRepository {
    data class Values(
        val dateFormatter: DateTimeFormatter,
    )

    fun currentDateFormat(): Flow<Values>
}