package com.eden.alarmplus.data.alarms.structure.state

/**
 *               ┏━━━━━━━━━━━🯟━━━━━━━━━━━┓
 *               ┃            ┏━━━━🯟━━━━━╋━━━━━━━━━━┓
 * PENDING━━🯟━━PRIMED━━🯟━━ACTIVE━━🯟━━DISMISSED━━🯟━━IDLE
 *     ┃         ┗━━━━━━━━━━━━━━━━🯝━━━━━━━━━━━━━━━━┛┃
 *     ┗━━━━━━━━━━━━━━━━━━━━━🯝━━━━━━━━━━━━━━━━━━━━━━┛
 */
enum class StateType {
    DISABLED,
    ENABLED_UNDEFINED,
    ENABLED_PENDING,
    ENABLED_ACTIVE,
    ENABLED_DISMISSED,
    ENABLED_IDLE,
    ENABLED_PRIMED,
}