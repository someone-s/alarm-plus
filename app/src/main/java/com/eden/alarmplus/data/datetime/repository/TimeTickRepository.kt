package com.eden.alarmplus.data.datetime.repository

import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime

interface TimeTickRepository {
    fun currentDateTime(): Flow<ZonedDateTime>
}