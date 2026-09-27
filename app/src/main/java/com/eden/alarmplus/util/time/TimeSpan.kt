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
fun isTimeWithinTimeSpan(
    timeSpan: List<LocalTime>,
    time: LocalTime
): Boolean {
    val inDuration: Boolean

    if (timeSpan.isEmpty()) {
        inDuration = false
    } else if (timeSpan.size == 1) {
        inDuration = time == timeSpan.first()
    } else {
        val startTime = timeSpan.first()
        val endTime = timeSpan.last()

        val isWrapAround = startTime.isAfter(endTime)

        val isAfterOrAtStart = time.isAfter(startTime) || time == startTime
        val isAfterOrAtEnd = time.isAfter(endTime) || time == endTime

        inDuration =
            if (isWrapAround) isAfterOrAtStart == isAfterOrAtEnd
            else isAfterOrAtStart != isAfterOrAtEnd
    }
    return inDuration
}