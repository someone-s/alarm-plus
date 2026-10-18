package com.eden.alarmplus.data.datetime.repository

import android.content.Context
import android.text.format.DateFormat
import com.eden.alarmplus.data.datetime.receiver.TimeChangeReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class ReceiverDateFormatRepository(
    private val context: Context,
    private val timeChangeReceiver: TimeChangeReceiver,
): DateFormatRepository {

    override fun currentDateFormat(): Flow<DateFormatRepository.Values> {
        return timeChangeReceiver.timeChanged.map {
            DateFormatRepository.Values(
                dateFormatter = DateTimeFormatter.ofPattern(
                    (DateFormat.getDateFormat(context) as SimpleDateFormat).toPattern())
            )
        }
    }

}