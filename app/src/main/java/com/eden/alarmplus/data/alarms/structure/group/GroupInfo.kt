package com.eden.alarmplus.data.alarms.structure.group

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Immutable
@Serializable
@Parcelize
data class GroupInfo(
    val uuid: Uuid = Uuid.random(),
    val name: String?,
    val enabled: Boolean,
) : Parcelable {
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
    enabled = true,
)

fun getUniqueGroupInfo() = GroupInfo(
    name = null,
    enabled = true,
)