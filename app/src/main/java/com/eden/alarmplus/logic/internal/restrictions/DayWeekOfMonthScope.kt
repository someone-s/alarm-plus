package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.DayWeekOfMonthFlags
import java.time.LocalDate
import java.time.temporal.ChronoField
import kotlin.collections.flatMap

class DayWeekOfMonthScope(
    private val flags: DayWeekOfMonthFlags
): RestrictionScope {
    override val filterOrder = 900
    override val defaultFilterLimit = 6

    override fun isDateAllowed(date: LocalDate): Boolean =
        flags.any(date.dayOfMonth, date.month.length(date.isLeapYear))

    override fun getStartDate() = null

    override fun filterStartDate(
        sorted: List<StartPeriod>,
        limit: Int?
    ): List<StartPeriod> = sorted
        .flatMap { period ->

            if (period.until == null) {

                fun nextOrSameDate(date: LocalDate): LocalDate {

                    val firstDateOfMonth = date.with(ChronoField.DAY_OF_MONTH, 1)

                    val dayWeekOfMonth = date.dayOfMonth / 7
                    val dayWeeksInMonth = date.month.length(date.isLeapYear)

                    val result = flags.nextOrSame(
                        dayWeekOfMonth = dayWeekOfMonth,
                        dayWeeksInMonth = dayWeeksInMonth,
                        dayWeeksInNextMonth = firstDateOfMonth
                            .plusMonths(1)
                            .let { it.month.length(it.isLeapYear) },
                    )
                    check(result.monthOffset >= 0)
                    check(result.dayWeekOfMonth >= 0)

                    return if (result.monthOffset == 0 && result.dayWeekOfMonth == dayWeekOfMonth) date
                    else firstDateOfMonth.plusMonths(result.monthOffset.toLong()).plusDays(7L * result.dayWeekOfMonth)
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
                    this.withDayOfMonth(this.dayOfMonth - this.dayOfMonth % 7)

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

                    if (flags.any(
                        dayWeekOfMonth = week.dayOfMonth / 7,
                        dayWeeksInMonth = week.month.length(week.isLeapYear)
                    )) {
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

    override fun getEndDate() = null

    override fun filterEndDate(
        sorted: List<EndPeriod>,
        limit: Int?
    ): List<EndPeriod> = sorted
        .flatMap { period ->

            if (period.since == null) {

                fun prevOrSameDate(date: LocalDate): LocalDate {

                    val firstDateOfMonth = date.with(ChronoField.DAY_OF_MONTH, 1)

                    val dayWeekOfMonth = date.dayOfMonth / 7
                    val dayWeeksInMonth = date.month.length(date.isLeapYear)

                    val result = flags.prevOrSame(
                        dayWeekOfMonth = dayWeekOfMonth,
                        dayWeeksInMonth = dayWeeksInMonth,
                        dayWeeksInPrevMonth = firstDateOfMonth
                            .minusMonths(1)
                            .let { it.month.length(it.isLeapYear) },
                    )
                    check(result.monthOffset >= 0)
                    check(result.dayWeekOfMonth >= 0)

                    return if (result.monthOffset == 0 && result.dayWeekOfMonth == dayWeekOfMonth) date
                    else firstDateOfMonth.plusMonths(result.monthOffset.toLong()).plusDays(7L * result.dayWeekOfMonth)
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

                fun LocalDate.snapToDayWeekStart() =
                    this.withDayOfMonth(this.dayOfMonth - this.dayOfMonth % 7)

                val startWeek = period.since.snapToDayWeekStart()
                val endWeek = period.on.snapToDayWeekStart()

                val output: MutableList<EndPeriod> =
                    if (limit != null) ArrayList(limit)
                    else ArrayList()

                var week = endWeek
                while (
                    week.isOnOrAfter(startWeek) &&
                    (limit == null || output.size < limit)
                ) {

                    val weekBefore = week.minusDays(1).snapToDayWeekStart()

                    if (flags.any(
                            dayWeekOfMonth = week.dayOfMonth / 7,
                            dayWeeksInMonth = week.month.length(week.isLeapYear)
                        )) {
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

                output.reversed()
            }
        }
}