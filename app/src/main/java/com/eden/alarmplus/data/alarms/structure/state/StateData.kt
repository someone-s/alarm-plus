package com.eden.alarmplus.data.alarms.structure.state

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid

/**
 *     ┌──────────🯟──────────┐
 * ACTIVE──🯟──DISMISSED──🯟──IDLE──🯟──PRIMED
 *    │           └────🯝───────────────┘│
 *    └────────────────🯝────────────────┘
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
        PRIMED,
    }

    fun toActive(): StateData {
        assert(this.type == Type.UNDEFINED || this.type == Type.PRIMED)
        return StateData(this.groupUuid, Type.ACTIVE)
    }
    fun toDismissed(): StateData {
        assert(this.type == Type.UNDEFINED || this.type == Type.ACTIVE || this.type == Type.PRIMED)
        return StateData(this.groupUuid, Type.DISMISSED)
    }
    fun toIdle(): StateData {
        assert(this.type == Type.UNDEFINED || this.type == Type.ACTIVE || this.type == Type.DISMISSED)
        return StateData(this.groupUuid, Type.IDLE)
    }
    fun toPrimed(): StateData {
        assert(this.type == Type.UNDEFINED || this.type == Type.IDLE)
        return StateData(this.groupUuid, Type.PRIMED)
    }
}