package com.eden.alarmplus.logic.internal.restrictions

import java.time.LocalDate

interface RestrictionScope {

    /**
     * Order restrictions are applied
     */
    val filterOrder: Int

    val defaultFilterLimit: Int?

    fun isDateAllowed(date: LocalDate): Boolean

    /**
     * Time element always set by [java.time.LocalTime.MIDNIGHT]
     */
    fun getStartDate(): LocalDate?

    /**
     * @param sorted ascending
     * @param limit null or >= 1
     */
    fun filterStartDate(sorted: List<StartPeriod>, limit: Int? = defaultFilterLimit): List<StartPeriod>

    /**
     * Time element always set by [java.time.LocalTime.MIDNIGHT]
     */
    fun getEndDate(): LocalDate?

    /**
     * @param sorted descending
     * @param limit null or >= 1
     */
    fun filterEndDate(sorted: List<EndPeriod>, limit: Int? = defaultFilterLimit): List<EndPeriod>
}