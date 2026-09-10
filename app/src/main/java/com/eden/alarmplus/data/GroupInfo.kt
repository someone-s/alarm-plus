package com.eden.alarmplus.data

data class GroupInfo(
    val name: String?,
    val active: Boolean,
) {
    fun shallowCopy(alarms: List<AlarmData>) = GroupInfo(
        name = this.name,
        active = this.active,
    )
}

val emptyGroupInfo = GroupInfo(
    name = null,
    active = false,
)