package com.eden.alarmplus.logic.internal

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.eden.alarmplus.data.alarms.repository.StateRepository
import com.eden.alarmplus.data.alarms.structure.group.feature.RestrictionFeature
import com.eden.alarmplus.data.alarms.structure.state.StateType
import com.eden.alarmplus.logic.internal.restrictions.RestrictionProviders
import com.eden.alarmplus.logic.internal.restrictions.RestrictionProviders.getFirstStartFrom
import com.eden.alarmplus.logic.internal.restrictions.RestrictionProviders.isDateAllowed
import com.eden.alarmplus.ui.main.MainActivity
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

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
        val fromTime = fromDateTime.toLocalTime()

        return stateRepository.getAllGroups()
            .mapNotNull { group ->
                when (group.info.state) {
                    StateType.DISABLED -> return@mapNotNull null
                    else -> {}
                }

                val info = group.info

                val restrictions = info
                    .getFeaturesOf<RestrictionFeature>()
                    .let { RestrictionProviders.getScopes(it) }

                val allowYesterday = restrictions.isDateAllowed(fromDate.minusDays(1))
                val allowToday = restrictions.isDateAllowed(fromDate)
                val allowTomorrow = restrictions.isDateAllowed(fromDate.plusDays(1))

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

                    val nextStartDate = fromDate.plusDays(1).let { adjusted ->
                        restrictions.getFirstStartFrom(adjusted).let { result ->
                            result.nextStartPeriod?.on ?: adjusted
                        }
                    }

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
                        ?.let { LocalDateTime.of(fromDate.plusDays(1), it.info.time) }
                        ?.minus(dismissWindow)
                        ?.also { if (it.isAfter(fromDateTime)) nextDismissDateTime = it }

                if (nextDismissDateTime == null) {

                    val nextDismissDate = fromDate.plusDays(2).let { adjusted ->
                        restrictions.getFirstStartFrom(adjusted).let { result ->
                            result.nextStartPeriod?.on ?: adjusted
                        }
                    }

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