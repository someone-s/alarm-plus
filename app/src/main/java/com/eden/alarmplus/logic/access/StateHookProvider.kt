package com.eden.alarmplus.logic.access

import java.time.LocalDateTime
import kotlin.uuid.Uuid

interface StateHookProvider {
    fun initializeState(dateTime: LocalDateTime)
    fun dismissGroup(groupUuid: Uuid)
}

