package com.eden.alarmplus.data.alarms.structure.group

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.eden.alarmplus.data.alarms.structure.state.StateType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Immutable
@Serializable
@Parcelize
data class GroupInfo(
    val uuid: Uuid = Uuid.random(),
    val name: String?,
    val features: List<GroupFeature>,
    val state: StateType = StateType.ENABLED_UNDEFINED,
) : Parcelable {
    fun shallowCopy(name: String?) = GroupInfo(
        uuid = this.uuid,
        name = name,
        features = this.features,
    )


    fun shallowCopyAddFeature(feature: GroupFeature) = GroupInfo(
        uuid = this.uuid,
        name = this.name,
        features = this.features + feature
    )

    inline fun <reified T: GroupFeature> shallowCopyRemoveFeature() = GroupInfo(
        uuid = this.uuid,
        name = this.name,
        features = this.features.filter { it !is T }
    )

    inline fun <reified T: GroupFeature> shallowCopyReplaceFeature(feature: T) = GroupInfo(
        uuid = this.uuid,
        name = this.name,
        features = this.features.filter { it !is T } + feature
    )

    inline fun <reified T: GroupFeature> getFeature(): T? {
        return features.firstOrNull { it is T } as? T
    }

}

val emptyGroupInfo = GroupInfo(
    name = null,
    features = emptyList(),
)

fun getUniqueGroupInfo() = GroupInfo(
    name = null,
    features = emptyList(),
)