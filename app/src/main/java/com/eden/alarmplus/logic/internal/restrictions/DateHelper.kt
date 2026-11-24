package com.eden.alarmplus.logic.internal.restrictions

import java.time.LocalDate

fun LocalDate.isOnOrBefore(date: LocalDate): Boolean = !this.isAfter(date)

fun LocalDate.isAfter(date: LocalDate): Boolean = this.isAfter(date)

fun LocalDate.isOnOrAfter(date: LocalDate): Boolean = !this.isBefore(date)

fun LocalDate.isBefore(date: LocalDate): Boolean = this.isBefore(date)

