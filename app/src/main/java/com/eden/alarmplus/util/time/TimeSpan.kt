package com.eden.alarmplus.util.time

import androidx.compose.runtime.Stable
import java.time.Duration
import java.time.LocalTime

@Stable
fun isTimeRightBeforeAndNotInSpan(
    timeSpan: List<LocalTime>,
    time: LocalTime,
    acceptedDuration: Duration
): Boolean {
    if (timeSpan.isEmpty())
        return false

    if (isTimeWithinTimeSpan(timeSpan, time))
        return false

    val wrappedDuration = Duration.between(time, timeSpan.first()).let {
        if (it.isNegative) it.plusDays(1)
        else it
    }

    if (wrappedDuration.isZero)
        return false

    return wrappedDuration <= acceptedDuration
}

@Stable
fun isTimeRightBeforeAndNotAnchor(
    anchor: LocalTime,
    window: Duration,
    time: LocalTime,
    allowToday: Boolean = true,
    allowTomorrow: Boolean = true,
): Boolean {
    val (wrappedDuration, forTomorrow) = Duration.between(time, anchor).let {
        if (it.isNegative) Pair(it.plusDays(1), true)
        else Pair(it, false)
    }

    if (wrappedDuration.isZero)
        return false

    return wrappedDuration <= window && ((forTomorrow && allowTomorrow) || (!forTomorrow && allowToday))
}

@Stable
fun isTimeWithinTimeSpan(
    timeSpan: List<LocalTime>,
    time: LocalTime,
    allowToday: Boolean = true,
    allowYesterday: Boolean = true,
): Boolean {
    val inDuration: Boolean

    if (timeSpan.isEmpty()) {
        inDuration = false
    } else if (timeSpan.size == 1) {
        inDuration = allowToday && time == timeSpan.first()
    } else {
        val startTime = timeSpan.first()
        val endTime = timeSpan.last()

        val isWrapAround = startTime.isAfter(endTime)

        val isAfterOrAtStart = time.isAfter(startTime) || time == startTime
        val isBeforeOrAtEnd = time.isBefore(endTime) || time == endTime
        val isAfterOrAtEnd = time.isAfter(endTime) || time == endTime

        inDuration =
            if (isWrapAround) (allowToday && isAfterOrAtStart) || (allowYesterday && isBeforeOrAtEnd)
            else allowToday && (isAfterOrAtStart != isAfterOrAtEnd)
    }
    return inDuration
}