package com.eden.alarmplus.data.group

import com.eden.alarmplus.data.alarm.AlarmData

data class GroupData(
    val info: GroupInfo,
    val alarms: List<AlarmData>,
)

val emptyGroupData = GroupData(
    info = emptyGroupInfo,
    alarms = emptyList(),
)