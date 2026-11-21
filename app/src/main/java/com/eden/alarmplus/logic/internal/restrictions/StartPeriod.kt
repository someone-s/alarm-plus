package com.eden.alarmplus.logic.internal.restrictions

import java.time.ZonedDateTime

data class StartPeriod(
    /**
     * Time element always set by [java.time.LocalTime.MIDNIGHT]
     */
    val on: ZonedDateTime,
    /**
     * Inclusive;
     * Time element always set by [java.time.LocalTime.MIDNIGHT]
     */
    val until: ZonedDateTime?,
)