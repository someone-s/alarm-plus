package com.eden.alarmplus.logic.internal.restrictions

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun LocalDate.toZonedDateTime(): ZonedDateTime =
    ZonedDateTime.of(this, LocalTime.MIDNIGHT, ZoneId.systemDefault())

fun ZonedDateTime.isOnOrBefore(date: LocalDate): Boolean = !this.isAfter(date.toZonedDateTime())

fun ZonedDateTime.isAfter(date: LocalDate): Boolean = this.isAfter(date.toZonedDateTime())

fun ZonedDateTime.isOnOrAfter(date: LocalDate): Boolean = !this.isBefore(date.toZonedDateTime())

fun ZonedDateTime.isBefore(date: LocalDate): Boolean = this.isBefore(date.toZonedDateTime())

