package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.DayWeekFromStartOfMonthFlags
import java.time.LocalDate
import java.time.temporal.ChronoField
import kotlin.collections.flatMap

class DayWeekFromStartOfMonthLogic(
    private val flags: DayWeekFromStartOfMonthFlags
) {
    private val LocalDate.dayWeekFromStartOfMonth get() = (this.dayOfMonth - 1) / 7

    fun isDateAllowed(date: LocalDate): Boolean =
        flags.any(date.dayWeekFromStartOfMonth)

    fun filterStartDate(
        sorted: List<StartPeriod>,
        limit: Int?
    ): List<StartPeriod> = sorted
        .flatMap { period ->

            if (period.until == null) {

                fun nextOrSameDate(date: LocalDate): LocalDate? {

                    val firstDateOfMonth = date.with(ChronoField.DAY_OF_MONTH, 1)

                    val result = flags.nextOrSame(date.dayWeekFromStartOfMonth) ?: return null

                    return if (result.monthOffset == 0 && result.dayWeekFromStartOfMonth == date.dayWeekFromStartOfMonth)
                        date
                    else
                        firstDateOfMonth
                            .plusMonths(result.monthOffset.toLong())
                            .plusDays(7L * result.dayWeekFromStartOfMonth)
                }

                val filteredOn = nextOrSameDate(period.on)
                buildList {
                    if (filteredOn != null) add(
                        StartPeriod(on = filteredOn, until = null)
                    )
                }
            }
            else {

                assert(period.until.isOnOrAfter(period.on))

                fun LocalDate.snapToDayWeekStart(): LocalDate =
                    this.withDayOfMonth(1 + 7 * this.dayWeekFromStartOfMonth)

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

                    if (flags.any(week.dayWeekFromStartOfMonth)) {
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

                fun prevOrSameDate(date: LocalDate): LocalDate? {

                    val firstDateOfMonth = date.with(ChronoField.DAY_OF_MONTH, 1)

                    val result = flags.prevOrSame(date.dayWeekFromStartOfMonth) ?: return null

                    return if (result.monthOffset == 0 && result.dayWeekFromStartOfMonth == date.dayWeekFromStartOfMonth)
                        date
                    else
                        firstDateOfMonth
                            .plusMonths(result.monthOffset.toLong())
                            .plusDays(7L * (result.dayWeekFromStartOfMonth + 1) - 1L)
                }

                val filteredOn = prevOrSameDate(period.on)
                buildList {
                    if (filteredOn != null) add(
                        EndPeriod(on = filteredOn, since = null)
                    )
                }
            }
            else {

                assert(period.since.isOnOrBefore(period.on))

                fun LocalDate.snapToDayWeekEnd() =
                    this.withDayOfMonth(7 * (this.dayWeekFromStartOfMonth + 1))

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

                    if (flags.any(week.dayWeekFromStartOfMonth)) {
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