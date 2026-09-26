package com.eden.alarmplus.logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.eden.alarmplus.data.alarms.repository.StateRepository
import com.eden.alarmplus.ui.main.MainActivity
import java.time.LocalDateTime
import java.time.ZoneId

class WakeScheduler(
    private val context: Context,
    private val stateRepository: StateRepository
) {

    suspend fun tryScheduleWake(fromDateTime: LocalDateTime = LocalDateTime.now()): Boolean {

        val nextDateTime = getNextDateTime(fromDateTime) ?: return false
        setWakeAlarm(nextDateTime)

        return true
    }

    private suspend fun getNextDateTime(fromDateTime: LocalDateTime): LocalDateTime? {

        val fromTime = fromDateTime.toLocalTime()
        val nextTime = stateRepository.findNextActiveAlarmTimeAfter(fromTime) ?: return null

        val nextDate = if (nextTime.isAfter(fromTime))
            fromDateTime.toLocalDate()
        else
            fromDateTime.toLocalDate().plusDays(1)

        val nextDateTime = LocalDateTime.of(nextDate, nextTime)

        return nextDateTime
    }

    private fun setWakeAlarm(dateTime: LocalDateTime) {
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
                putExtra(WakeReceiver.EXTRA_TO_DATE_TIME, dateTime.toString())
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT,
        )

        val zonedToDateTime = dateTime.atZone(ZoneId.systemDefault())
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