package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.max
import kotlin.math.min

class DayOfWeekScope(
    private val flags: DayOfWeekFlags
): RestrictionScope {
    override val filterOrder = 1000

    override val defaultFilterLimit = 7L

    override fun isDateAllowed(date: LocalDate): Boolean = flags.any(date.dayOfWeek)

    override fun getStartDate(): ZonedDateTime? = null

    override fun filterStartDate(sorted: List<StartPeriod>, limit: Long?): List<StartPeriod> =
        sorted.flatMap { startDate ->
            if (startDate.until == null)
                listOf(
                    StartPeriod(
                        on = flags.toDayOfWeeks()
                            .map { startDate.on.with(TemporalAdjusters.nextOrSame(it)) }
                            .minBy { it },
                        until = null,
                    )
                )
            else if (flags.all(DayOfWeekFlags.ALL))
                listOf(startDate)
            else {
                val days = 1 + ChronoUnit.DAYS.between(startDate.on, startDate.until)
                val count = if (limit != null) min(days, limit) else days
                (0..<count).mapNotNull { dayIndex ->
                    val date = startDate.on.plusDays(dayIndex)
                    if (flags.any(date.dayOfWeek))
                        StartPeriod(
                            on = date,
                            until = date,
                        )
                    else null
                }
            }
        }

    override fun getEndDate(): ZonedDateTime? = null

    override fun filterEndDate(sorted: List<EndPeriod>, limit: Long?): List<EndPeriod> =
        sorted.flatMap { endDate ->
            if (endDate.since == null)
                listOf(
                    EndPeriod(
                        on = flags.toDayOfWeeks()
                            .map { endDate.on.with(TemporalAdjusters.previousOrSame(it)) }
                            .maxOf { it },
                        since = null,
                    )
                )
            else if (flags.all(DayOfWeekFlags.ALL))
                listOf(endDate)
            else {
                val days = 1 + ChronoUnit.DAYS.between(endDate.since, endDate.on)
                val first = if (limit != null) max(0, days - limit) else 0
                (first..<days).mapNotNull { dayIndex ->
                    val date = endDate.since.plusDays(dayIndex)
                    if (flags.any(date.dayOfWeek))
                        EndPeriod(
                            on = date,
                            since = date,
                        )
                    else null
                }
            }
        }
}