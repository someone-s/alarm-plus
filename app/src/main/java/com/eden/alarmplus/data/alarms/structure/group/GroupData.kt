package com.eden.alarmplus.data.alarms.structure.group

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class GroupData(
    val info: GroupInfo,
    val alarms: List<AlarmData>,
    val dayOfWeekFlags: DayOfWeekFlags,
) : Parcelable {
    fun shallowCopy(info: GroupInfo) = GroupData(
        info = info,
        alarms = this.alarms,
        dayOfWeekFlags = this.dayOfWeekFlags,
    )
}

val emptyGroupData = GroupData(
    info = emptyGroupInfo,
    alarms = emptyList(),
    dayOfWeekFlags = DayOfWeekFlags.ALL,
)

fun getUniqueGroupData() = GroupData(
    info = getUniqueGroupInfo(),
    alarms = emptyList(),
    dayOfWeekFlags = DayOfWeekFlags.ALL,
)