package com.eden.alarmplus.data.group

data class GroupInfo(
    val name: String?,
    val active: Boolean,
)

val emptyGroupInfo = GroupInfo(
    name = null,
    active = false,
)