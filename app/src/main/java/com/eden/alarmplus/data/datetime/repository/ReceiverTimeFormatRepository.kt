package com.eden.alarmplus.data.datetime.repository

import android.content.Context
import android.text.format.DateFormat
import com.eden.alarmplus.data.datetime.receiver.TimeChangeReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class ReceiverTimeFormatRepository(
    private val context: Context,
    private val timeChangeReceiver: TimeChangeReceiver,
): TimeFormatRepository {

    override fun is24Formatted(): Flow<Boolean> {
        return timeChangeReceiver.timeChanged.map { DateFormat.is24HourFormat(context) }
    }

    override fun currentTimeFormat(): Flow<DateTimeFormatter> {
        return timeChangeReceiver.timeChanged.map {
            DateTimeFormatter.ofPattern(
                (DateFormat.getTimeFormat(context) as SimpleDateFormat).toPattern()
            )
        }
    }

}