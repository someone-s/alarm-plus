package com.eden.alarmplus.logic.internal.restrictions

import java.time.LocalDate
import java.time.ZonedDateTime

class ScheduleEndScope(
    private val endDate: LocalDate
): RestrictionScope {

    override val filterOrder: Int = 2

    override val defaultFilterLimit = null

    override fun isDateAllowed(date: LocalDate): Boolean = !date.isAfter(endDate)

    override fun getStartDate(): ZonedDateTime? = null

    override fun filterStartDate(sorted: List<StartPeriod>, limit: Long?): List<StartPeriod> {
        val end = endDate.toZonedDateTime()
        return sorted.mapNotNull { startDate ->
            if (startDate.on.isAfter(end))
                null
            else
                StartPeriod(
                    on = startDate.on,
                    until =
                        if (startDate.until == null) end
                        else minOf(end, startDate.until)
                )
        }
    }

    override fun getEndDate() = endDate.toZonedDateTime()

    override fun filterEndDate(sorted: List<EndPeriod>, limit: Long?): List<EndPeriod> {
        val end = endDate.toZonedDateTime()
        return sorted.mapNotNull { endDate ->
            if (endDate.since != null &&
                endDate.since.isAfter(end))
                null
            else
                EndPeriod(
                    on = minOf(end, endDate.on),
                    since = endDate.since
                )
        }
    }
}