package com.eden.alarmplus.data.alarms.structure.group

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid

@Immutable
data class GroupInfo(
    val uuid: Uuid = Uuid.random(),
    val name: String?,
    val enabled: Boolean,
) {
    fun shallowCopy(name: String?) = GroupInfo(
        uuid = this.uuid,
        name = name,
        enabled = this.enabled,
    )

    fun shallowCopy(active: Boolean) = GroupInfo(
        uuid = this.uuid,
        name = this.name,
        enabled = active,
    )
}

val emptyGroupInfo = GroupInfo(
    name = null,
    enabled = false,
)

fun getUniqueGroupInfo() = GroupInfo(
    name = null,
    enabled = false,
)