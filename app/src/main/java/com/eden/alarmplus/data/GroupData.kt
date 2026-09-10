package com.eden.alarmplus.data

data class GroupData(
    val info: GroupInfo,
    val alarms: List<AlarmData>,
) {
    fun shallowCopy(alarms: List<AlarmData>) = GroupData(
        info = this.info,
        alarms = alarms,
    )
}

val emptyGroupData = GroupData(
    info = emptyGroupInfo,
    alarms = emptyList(),
)