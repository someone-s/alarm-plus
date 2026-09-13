package com.eden.alarmplus.data.group

import kotlin.uuid.Uuid

data class GroupInfo(
    val uuid: Uuid = Uuid.random(),
    val name: String?,
    val active: Boolean,
)

val emptyGroupInfo = GroupInfo(
    name = null,
    active = false,
)