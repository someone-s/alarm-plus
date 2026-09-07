package com.eden.alarmplus.data

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