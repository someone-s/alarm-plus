package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.DayWeekFromEndOfMonthFlags
import java.time.LocalDate
import java.time.temporal.ChronoField
import kotlin.collections.flatMap

class DayWeekFromEndOfMonthLogic(
    private val flags: DayWeekFromEndOfMonthFlags
) {
    private val LocalDate.lastDayOfMonth get() = this.month.length(this.isLeapYear)
    private val LocalDate.dayWeekFromEndOfMonth get() = (this.month.length(this.isLeapYear) - this.dayOfMonth) / 7

    fun isDateAllowed(date: LocalDate): Boolean =
        flags.any(date.dayWeekFromEndOfMonth)

    fun filterStartDate(
        sorted: List<StartPeriod>,
        limit: Int?
    ): List<StartPeriod> = sorted
        .flatMap { period ->

            if (period.until == null) {

                fun nextOrSameDate(date: LocalDate): LocalDate {

                    val firstDateOfMonth = date.with(ChronoField.DAY_OF_MONTH, 1)

                    val result = flags.nextOrSame(date.dayWeekFromEndOfMonth)

                    return if (result.monthOffset == 0 && result.dayWeekFromEndOfMonth == date.dayWeekFromEndOfMonth)
                        date
                    else
                        firstDateOfMonth
                            .plusMonths(result.monthOffset.toLong() + 1L)
                            .minusDays(7L * (result.dayWeekFromEndOfMonth + 1))
                }

                listOf(
                    StartPeriod(
                        on = nextOrSameDate(period.on),
                        until = null,
                    )
                )
            }
            else {

                assert(period.until.isOnOrAfter(period.on))

                fun LocalDate.snapToDayWeekStart() =
                    this.withDayOfMonth(this.lastDayOfMonth - 6 - 7 * this.dayWeekFromEndOfMonth)

                val startWeek = period.on.snapToDayWeekStart()
                val endWeek = period.until.snapToDayWeekStart()

                val output: MutableList<StartPeriod> =
                    if (limit != null) ArrayList(limit)
                    else ArrayList()

                var week = startWeek
                while (
                    week.isOnOrBefore(endWeek) &&
                    (limit == null || output.size < limit)
                ) {

                    val weekAfter = week.plusWeeks(1).snapToDayWeekStart()

                    if (flags.any(week.dayWeekFromEndOfMonth)) {
                        output.add(
                            StartPeriod(
                                on =
                                    if (week.isEqual(startWeek)) period.on
                                    else week,
                                until =
                                    if (week.isEqual(endWeek)) period.until
                                    else weekAfter.minusDays(1)
                            )
                        )
                    }

                    week = weekAfter
                }

                output
            }
        }

    fun filterEndDate(
        sorted: List<EndPeriod>,
        limit: Int?
    ): List<EndPeriod> = sorted
        .flatMap { period ->

            if (period.since == null) {

                fun prevOrSameDate(date: LocalDate): LocalDate {

                    val firstDateOfMonth = date.with(ChronoField.DAY_OF_MONTH, 1)

                    val result = flags.prevOrSame(date.dayWeekFromEndOfMonth)

                    return if (result.monthOffset == 0 && result.dayWeekFromEndOfMonth == date.dayWeekFromEndOfMonth)
                        date
                    else
                        firstDateOfMonth
                            .plusMonths(result.monthOffset.toLong() + 1L)
                            .minusDays(7L * (result.dayWeekFromEndOfMonth + 1))
                }

                listOf(
                    EndPeriod(
                        on = prevOrSameDate(period.on),
                        since = null,
                    )
                )
            }
            else {

                assert(period.since.isOnOrBefore(period.on))

                fun LocalDate.snapToDayWeekEnd() =
                    this.withDayOfMonth(this.lastDayOfMonth - 7 * this.dayWeekFromEndOfMonth)

                val startWeek = period.since.snapToDayWeekEnd()
                val endWeek = period.on.snapToDayWeekEnd()

                val output: MutableList<EndPeriod> =
                    if (limit != null) ArrayList(limit)
                    else ArrayList()

                var week = endWeek
                while (
                    week.isOnOrAfter(startWeek) &&
                    (limit == null || output.size < limit)
                ) {

                    val weekBefore = week.minusWeeks(1).snapToDayWeekEnd()

                    if (flags.any(week.dayWeekFromEndOfMonth)) {
                        output.add(
                            EndPeriod(
                                on =
                                    if (week.isEqual(endWeek)) period.on
                                    else week,
                                since =
                                    if (week.isEqual(startWeek)) period.since
                                    else weekBefore.plusDays(1)
                            )
                        )
                    }

                    week = weekBefore
                }

                output
            }
        }
}