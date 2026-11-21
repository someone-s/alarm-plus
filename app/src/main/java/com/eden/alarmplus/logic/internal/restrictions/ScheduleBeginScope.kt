package com.eden.alarmplus.logic.internal.restrictions

import java.time.LocalDate

class ScheduleBeginScope(
    private val beginDate: LocalDate
): RestrictionScope {

    override val filterOrder = 1

    override val defaultFilterLimit = null

    override fun isDateAllowed(date: LocalDate): Boolean = !date.isBefore(beginDate)

    override fun getStartDate() = beginDate

    override fun filterStartDate(
        sorted: List<StartPeriod>,
        limit: Long?
    ): List<StartPeriod> = sorted
        .mapNotNull { startDate ->
            if (startDate.until != null &&
                startDate.until.isBefore(beginDate))
                null
            else
                StartPeriod(
                    on = maxOf(beginDate, startDate.on),
                    until = startDate.until
                )
        }

    override fun getEndDate() = null

    override fun filterEndDate(
        sorted: List<EndPeriod>,
        limit: Long?
    ): List<EndPeriod> = sorted
        .mapNotNull { endDate ->
            if (endDate.on.isBefore(beginDate))
                null
            else
                EndPeriod(
                    on = endDate.on,
                    since =
                        if (endDate.since == null) beginDate
                        else maxOf(beginDate, endDate.since)
                )
        }
}