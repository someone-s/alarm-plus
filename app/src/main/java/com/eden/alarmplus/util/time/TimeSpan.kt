package com.eden.alarmplus.util.time

import androidx.compose.runtime.Stable
import java.time.LocalTime

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