package com.eden.alarmplus.logic.internal.restrictions

import java.time.LocalDate
import java.time.ZonedDateTime

class ScheduleBeginScope(
    private val beginDate: LocalDate
): RestrictionScope {

    override val filterOrder: Int = 1
    override fun isDateAllowed(date: LocalDate): Boolean = !date.isBefore(beginDate)

    override fun getStartDate() = beginDate.toZonedDateTime()

    override fun filterStartDate(sorted: List<StartPeriod>, limit: Long?): List<StartPeriod> {
        val begin = beginDate.toZonedDateTime()
        return sorted.mapNotNull { startDate ->
            if (startDate.until != null &&
                startDate.until.isBefore(begin))
                null
            else
                StartPeriod(
                    on = maxOf(begin, startDate.on),
                    until = startDate.until
                )
        }
    }

    override fun getEndDate(): ZonedDateTime? = null

    override fun filterEndDate(sorted: List<EndPeriod>, limit: Long?): List<EndPeriod> {
        val begin = beginDate.toZonedDateTime()
        return sorted.mapNotNull { endDate ->
            if (endDate.on.isBefore(begin))
                null
            else
                EndPeriod(
                    on = endDate.on,
                    since =
                        if (endDate.since == null) begin
                        else maxOf(begin, endDate.since)
                )
        }
    }
}