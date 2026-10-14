package com.eden.alarmplus.logic.internal

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.eden.alarmplus.data.alarms.repository.StateRepository
import com.eden.alarmplus.ui.main.MainActivity
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class WakeScheduler(
    private val context: Context,
    private val stateRepository: StateRepository,
    private val dismissWindow: Duration,
) {

    suspend fun tryScheduleWake(fromDateTime: LocalDateTime = LocalDateTime.now()): Boolean {

        val nextDateTimeInfo = getNextDateTime(fromDateTime)

        if (nextDateTimeInfo != null) {
            setWakeAlarm(nextDateTimeInfo.localDateTime, nextDateTimeInfo.hasAlarmAtTime)
            return true
        }
        else {
            clearWakeAlarm()
            return false
        }
    }

    private data class NextDateTimeInfo(
        val localDateTime: LocalDateTime,
        val hasAlarmAtTime: Boolean,
    )

    private suspend fun getNextDateTime(fromDateTime: LocalDateTime): NextDateTimeInfo? {

        val fromDate = fromDateTime.toLocalDate()
        val fromDateAfter = fromDate.plusDays(1)
        val fromTime = fromDateTime.toLocalTime()
        val fromDayInWeek = fromDateTime.dayOfWeek
        val fromDayBeforeInWeek = fromDayInWeek.minus(1)
        val fromDayAfterInWeek = fromDayInWeek.plus(1)

        return stateRepository.getAllGroups()
            .filter { it.info.enabled }
            .mapNotNull { group ->
                val allowYesterday = group.dayOfWeekFlags.any(fromDayBeforeInWeek)
                val allowToday = group.dayOfWeekFlags.any(fromDayInWeek)
                val allowTomorrow = group.dayOfWeekFlags.any(fromDayAfterInWeek)

                val nextAlarmDateTime = if (allowYesterday && allowToday) {
                    group.alarms.map { it.info.time }
                        .firstOrNull { it.isAfter(fromTime) }
                        ?.let { LocalDateTime.of(fromDate, it) }
                }
                else if (allowYesterday) {
                    group.alarms.map { it.info.time }
                        .filter { it.isBefore(group.alarms.first().info.time) }
                        .firstOrNull { it.isAfter(fromTime) }
                        ?.let { LocalDateTime.of(fromDate, it) }
                }
                else if (allowToday) {
                    group.alarms.map { it.info.time }
                        .filter { !it.isBefore(group.alarms.first().info.time) }
                        .firstOrNull { it.isAfter(fromTime) }
                        ?.let { LocalDateTime.of(fromDate, it) }
                }
                else {
                    val nextStartDate = group.dayOfWeekFlags.toDayOfWeeks()
                        .map { fromDate.with(TemporalAdjusters.next(it)) }
                        .minByOrNull { it }
                    if (nextStartDate != null)
                        group.alarms.firstOrNull()
                            ?.let { LocalDateTime.of(nextStartDate, it.info.time) }
                    else null
                }

                var nextDismissDateTime: LocalDateTime? = null
                if (allowToday)
                    group.alarms.firstOrNull()
                        ?.let { LocalDateTime.of(fromDate, it.info.time) }
                        ?.minus(dismissWindow)
                        ?.also { if (it.isAfter(fromDateTime)) nextDismissDateTime = it }

                if (nextDismissDateTime == null && allowTomorrow)
                    group.alarms.firstOrNull()
                        ?.let { LocalDateTime.of(fromDateAfter, it.info.time) }
                        ?.minus(dismissWindow)
                        ?.also { if (it.isAfter(fromDateTime)) nextDismissDateTime = it }

                if (nextDismissDateTime == null) {
                    val nextDismissDate = group.dayOfWeekFlags.toDayOfWeeks()
                        .map { fromDateAfter.with(TemporalAdjusters.next(it)) }
                        .minByOrNull { it }
                    if (nextDismissDate != null)
                        group.alarms.firstOrNull()
                            ?.let { LocalDateTime.of(nextDismissDate, it.info.time) }
                            ?.minus(dismissWindow)
                            ?.also { nextDismissDateTime = it }
                }

                if (nextAlarmDateTime != null && nextDismissDateTime != null) {
                    val nextDateTime = minOf(nextAlarmDateTime, nextDismissDateTime)
                    Pair(nextDateTime, nextDateTime.isEqual(nextAlarmDateTime))
                }
                else if (nextAlarmDateTime != null)
                    Pair(nextAlarmDateTime, true)
                else if (nextDismissDateTime != null)
                    Pair(nextDismissDateTime, false)
                else
                    null
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            .minByOrNull { it.key }
            ?.let { NextDateTimeInfo(
                localDateTime = it.key,
                hasAlarmAtTime = it.value.any { isAlarm -> isAlarm }
            ) }


    }

    private fun createReceiverIntent(dateTime: LocalDateTime?) = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, WakeReceiver::class.java).apply {
            if (dateTime != null)
                putExtra(WakeReceiver.EXTRA_TO_DATE_TIME, dateTime.toString())
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT,
    )

    private fun clearWakeAlarm() {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
            ?: throw NoAlarmManagerException()

        alarmManager.cancel(createReceiverIntent(null))
    }

    private fun setWakeAlarm(dateTime: LocalDateTime, hasAlarmAtTime: Boolean) {

        val alarmManager = context.getSystemService(AlarmManager::class.java)
            ?: throw NoAlarmManagerException()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms())
                throw CannotScheduleExactAlarmException()
        }

        val zonedToDateTime = dateTime.atZone(ZoneId.systemDefault())
        val triggerTime = zonedToDateTime.toInstant().toEpochMilli()

        val receiverIntent = createReceiverIntent(dateTime)

        if (hasAlarmAtTime) {
            val showIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )

            // as the receiverIntent is exactly the same
            // (extras are not considered by comparing),
            // previously set alarm will be overwritten
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(
                    triggerTime,
                    showIntent,
                ),
                receiverIntent
            )
        }
        else {
            // as the receiverIntent is exactly the same
            // (extras are not considered by comparing),
            // previously set alarm will be overwritten
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                receiverIntent
            )
        }
    }

    class NoAlarmManagerException: RuntimeException("No alarm manager found")
    class CannotScheduleExactAlarmException: RuntimeException("Does not have permission to schedule exact alarms")
}