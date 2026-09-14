package com.eden.alarmplus.structure.group

import com.eden.alarmplus.structure.alarm.AlarmData

data class GroupData(
    val info: GroupInfo,
    val alarms: List<AlarmData>,
) {
    fun shallowCopy(info: GroupInfo) = GroupData(
        info = info,
        alarms = this.alarms,
    )
}

val emptyGroupData = GroupData(
    info = emptyGroupInfo,
    alarms = emptyList(),
)