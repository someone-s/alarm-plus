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
    /**
     * Used when manually triggered enabled (i.e. user toggle);
     * Interpreted as try to update other state to make sure the group is enabled
     */
    ENABLED_UNDEFINED_MANUAL,
    ENABLED_UNDEFINED_DEFAULT,
    ENABLED_PENDING,
    ENABLED_ACTIVE,
    ENABLED_DISMISSED,
    ENABLED_IDLE,
    ENABLED_PRIMED,
}