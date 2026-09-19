package com.eden.alarmplus.structure.group

import kotlin.uuid.Uuid

data class GroupState(
    val dataUuid: Uuid,
    val currentAlarmIndex: Int,
)