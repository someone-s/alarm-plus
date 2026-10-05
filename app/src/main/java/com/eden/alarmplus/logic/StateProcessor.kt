package com.eden.alarmplus.logic

import java.time.LocalDateTime
import kotlin.uuid.Uuid

interface StateProcessor {
    suspend fun initializeState(dateTime: LocalDateTime)
    suspend fun iterateState(dateTime: LocalDateTime)
    suspend fun dismissGroup(groupUuid: Uuid)
}

