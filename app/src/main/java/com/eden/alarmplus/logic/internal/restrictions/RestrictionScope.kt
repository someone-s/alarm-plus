package com.eden.alarmplus.logic.internal.restrictions

import java.time.LocalDate
import java.time.ZonedDateTime

interface RestrictionScope {

    /**
     * Order restrictions are applied
     */
    val filterOrder: Int

    val defaultFilterLimit: Long?

    fun isDateAllowed(date: LocalDate): Boolean

    /**
     * Time element always set by [java.time.LocalTime.MIDNIGHT]
     */
    fun getStartDate(): ZonedDateTime?

    /**
     * @param limit null or >= 1
     */
    fun filterStartDate(sorted: List<StartPeriod>, limit: Long? = defaultFilterLimit): List<StartPeriod>

    /**
     * Time element always set by [java.time.LocalTime.MIDNIGHT]
     */
    fun getEndDate(): ZonedDateTime?

    /**
     * @param limit null or >= 1
     */
    fun filterEndDate(sorted: List<EndPeriod>, limit: Long? = defaultFilterLimit): List<EndPeriod>
}