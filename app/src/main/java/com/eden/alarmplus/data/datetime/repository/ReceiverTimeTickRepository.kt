package com.eden.alarmplus.data.datetime.repository

import com.eden.alarmplus.data.datetime.receiver.TimeTickReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZonedDateTime

class ReceiverTimeTickRepository(
    private val timeTickReceiver: TimeTickReceiver,
): TimeTickRepository {
    override fun currentDateTime(): Flow<ZonedDateTime> {
        return timeTickReceiver.timeTicked.map { ZonedDateTime.now() }
    }
}