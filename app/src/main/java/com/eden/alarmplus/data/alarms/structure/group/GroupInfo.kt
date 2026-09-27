package com.eden.alarmplus.data.alarms.structure.group

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid

@Immutable
data class GroupInfo(
    val uuid: Uuid = Uuid.random(),
    val name: String?,
    val active: Boolean,
) {
    fun shallowCopy(name: String?) = GroupInfo(
        uuid = this.uuid,
        name = name,
        active = this.active,
    )

    fun shallowCopy(active: Boolean) = GroupInfo(
        uuid = this.uuid,
        name = this.name,
        active = active,
    )
}

val emptyGroupInfo = GroupInfo(
    name = null,
    active = false,
)

fun getUniqueGroupInfo() = GroupInfo(
    name = null,
    active = false,
)