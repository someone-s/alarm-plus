package com.eden.alarmplus.data.alarms.structure.state

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid

/**
 *     ┌──────────🯟──────────┐
 * ACTIVE──🯟──DISMISSED──🯟──IDLE
 *     │          └────🯝────┘│
 *     └──────────🯝──────────┘
 */
@Immutable
data class StateData(
    val groupUuid: Uuid,
    val type: Type,
) {

    enum class Type {
        UNDEFINED,
        ACTIVE,
        DISMISSED,
        IDLE,
    }

    fun toActive(): StateData {
        assert(this.type == Type.UNDEFINED || this.type == Type.IDLE)
        return StateData(groupUuid, Type.ACTIVE)
    }
    fun toDismissed(): StateData {
        assert(this.type == Type.UNDEFINED || this.type == Type.ACTIVE || this.type == Type.IDLE)
        return StateData(groupUuid, Type.DISMISSED)
    }
    fun toIdle(): StateData {
        assert(this.type == Type.UNDEFINED || this.type == Type.ACTIVE || this.type == Type.DISMISSED)
        return StateData(groupUuid, Type.IDLE)
    }
}