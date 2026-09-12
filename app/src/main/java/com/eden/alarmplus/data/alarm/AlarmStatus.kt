package com.eden.alarmplus.data.alarm

/**
 * Pending ━━━> In-Progress ━━━> Dismissed
 *    ^             ^                 ┃ ┃
 *    ┃             ┗━━━━━━━━━━━━━━━━━┛ ┃
 *    ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 */
enum class AlarmStatus {
    DISABLED,
    PENDING,
    IN_PROGRESS,
    DISMISSED,
}