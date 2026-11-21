package com.eden.alarmplus.logic.internal.restrictions

import java.time.LocalDate

data class EndPeriod(
    /**
     * Time element always set by [java.time.LocalTime.MIDNIGHT]
     */
    val on: LocalDate,
    /**
     * Inclusive;
     * Time element always set by [java.time.LocalTime.MIDNIGHT]
     */
    val since: LocalDate?,
)
