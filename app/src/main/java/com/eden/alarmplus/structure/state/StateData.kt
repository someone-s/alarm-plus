package com.eden.alarmplus.structure.state

import kotlin.uuid.Uuid

/**
 *     ┌──────────🯟──────────┐
 * ACTIVE──🯟──DISMISSED──🯟──IDLE
 *     │          └────🯝────┘│
 *     └──────────🯝──────────┘
 */
sealed class StateData
{
    abstract val groupUuid: Uuid

    data class Undefined(
        override val groupUuid: Uuid,
    ): StateData() {
        fun toActive() = Active(groupUuid)
        fun toDismissed() = Dismissed(groupUuid)
        fun toIdle() = Idle(groupUuid)
    }

    data class Active(
        override val groupUuid: Uuid,
    ): StateData() {
        fun toDismissed() = Dismissed(groupUuid)
        fun toIdle() = Idle(groupUuid)
    }

    data class Dismissed(
        override val groupUuid: Uuid,
    ): StateData() {
        fun toIdle() = Idle(groupUuid)
    }

    data class Idle(
        override val groupUuid: Uuid,
    ): StateData() {
        fun toActive() = Active(groupUuid)
        fun toDismissed() = Dismissed(groupUuid)
    }
}