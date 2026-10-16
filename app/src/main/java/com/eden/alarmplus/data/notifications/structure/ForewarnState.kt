package com.eden.alarmplus.data.notifications.structure

import kotlin.uuid.Uuid

data class ForewarnState(
    val groupUuid: Uuid,
    val notificationId: Int,
)