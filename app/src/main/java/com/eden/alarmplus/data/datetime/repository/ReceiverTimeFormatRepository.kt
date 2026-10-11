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

    override fun currentTimeFormat(): Flow<TimeFormatRepository.Values> {
        return timeChangeReceiver.timeChanged.map {
            TimeFormatRepository.Values(
                is24Formatted = DateFormat.is24HourFormat(context),
                timeFormatter = DateTimeFormatter.ofPattern(
                    (DateFormat.getTimeFormat(context) as SimpleDateFormat).toPattern())
            )
        }
    }

}