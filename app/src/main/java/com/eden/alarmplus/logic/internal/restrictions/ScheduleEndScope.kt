package com.eden.alarmplus.logic.internal.restrictions

import java.time.LocalDate

class ScheduleEndScope(
    private val endDate: LocalDate
): RestrictionScope {

    override val filterOrder: Int = 2

    override val defaultFilterLimit = null

    override fun isDateAllowed(date: LocalDate): Boolean = !date.isAfter(endDate)

    override fun getStartDate() = null

    override fun filterStartDate(
        sorted: List<StartPeriod>,
        limit: Int?
    ): List<StartPeriod> = sorted
        .mapNotNull { period ->
            if (period.on.isAfter(endDate))
                null
            else
                StartPeriod(
                    on = period.on,
                    until =
                        if (period.until == null) endDate
                        else minOf(endDate, period.until)
                )
        }

    override fun getEndDate() = endDate

    override fun filterEndDate(
        sorted: List<EndPeriod>,
        limit: Int?
    ): List<EndPeriod> = sorted
        .mapNotNull { period ->
            if (period.since != null &&
                period.since.isAfter(endDate))
                null
            else
                EndPeriod(
                    on = minOf(endDate, period.on),
                    since = period.since
                )
        }
}