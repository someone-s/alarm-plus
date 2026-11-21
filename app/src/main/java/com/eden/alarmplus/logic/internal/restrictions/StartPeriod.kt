package com.eden.alarmplus.logic.internal.restrictions

import java.time.LocalDate

data class StartPeriod(
    val on: LocalDate,
    /**
     * Inclusive
     */
    val until: LocalDate?,
)