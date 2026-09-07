package com.eden.alarmplus.data

data class GroupData(
    val name: String?,
    val status: GroupStatus,
    val alarms: List<AlarmData>,
)

val emptyGroupData = GroupData(
    name = null,
    status = GroupStatus.DISABLED,
    alarms = emptyList(),
)