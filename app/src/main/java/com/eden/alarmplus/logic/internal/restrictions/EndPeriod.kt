package com.eden.alarmplus.logic.internal.restrictions

import java.time.ZonedDateTime

data class EndPeriod(
    /**
     * Time element always set by [java.time.LocalTime.MIDNIGHT]
     */
    val on: ZonedDateTime,
    /**
     * Inclusive;
     * Time element always set by [java.time.LocalTime.MIDNIGHT]
     */
    val since: ZonedDateTime?,
)
