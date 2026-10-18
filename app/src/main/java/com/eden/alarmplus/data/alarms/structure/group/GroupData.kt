package com.eden.alarmplus.data.alarms.structure.group

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class GroupData(
    val info: GroupInfo,
    val alarms: List<AlarmData>,
) : Parcelable {
    fun shallowCopy(info: GroupInfo) = GroupData(
        info = info,
        alarms = this.alarms,
    )
}

val emptyGroupData = GroupData(
    info = emptyGroupInfo,
    alarms = emptyList(),
)

fun getUniqueGroupData() = GroupData(
    info = getUniqueGroupInfo(),
    alarms = emptyList(),
)