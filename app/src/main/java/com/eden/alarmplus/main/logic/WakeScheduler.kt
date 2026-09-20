package com.eden.alarmplus.main.logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.eden.alarmplus.main.MainActivity
import com.eden.alarmplus.main.MainApplication
import java.time.LocalDateTime
import java.time.ZoneId

class WakeScheduler(private val context: Context) {

    suspend fun scheduleWake(fromDateTime: LocalDateTime = LocalDateTime.now()) {

        val nextDateTime = getNextDateTime(fromDateTime)
        setWakeAlarm(fromDateTime, nextDateTime)
    }

    private suspend fun getNextDateTime(fromDateTime: LocalDateTime): LocalDateTime {
        val mainApplication = context.applicationContext as MainApplication

        val fromTime = fromDateTime.toLocalTime()
        val nextTime = mainApplication.container.groupAlarmsRepository.getNextAlarmTimeAfter(fromTime)

        val nextDate = if (nextTime.isAfter(fromTime))
            fromDateTime.toLocalDate()
        else
            fromDateTime.toLocalDate().plusDays(1)

        val nextDateTime = LocalDateTime.of(nextDate, nextTime)

        return nextDateTime
    }

    private fun setWakeAlarm(fromDateTime: LocalDateTime, toDateTime: LocalDateTime) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
            ?: throw NoAlarmManagerException()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms())
                throw CannotScheduleExactAlarmException()
        }

        val showIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        val receiverIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, WakeReceiver::class.java).apply {
                putExtra(WakeReceiver.EXTRA_FROM_DATE_TIME, fromDateTime.toString())
                putExtra(WakeReceiver.EXTRA_TO_DATE_TIME, toDateTime.toString())
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT,
        )

        val zonedToDateTime = toDateTime.atZone(ZoneId.systemDefault())
        val triggerTime = zonedToDateTime.toInstant().toEpochMilli()

        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerTime,
            showIntent,
        )

        // as the receiverIntent is exactly the same
        // (extras are not considered by comparing),
        // previously set alarm will be overwritten
        alarmManager.setAlarmClock(
            alarmClockInfo,
            receiverIntent,
        )
    }

    class NoAlarmManagerException: RuntimeException("No alarm manager found")
    class CannotScheduleExactAlarmException: RuntimeException("Does not have permission to schedule exact alarms")
}