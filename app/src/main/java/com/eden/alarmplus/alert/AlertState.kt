package com.eden.alarmplus.alert

import android.app.Notification
import kotlin.uuid.Uuid

data class AlertState(
    val groupUuid: Uuid,
    val notificationId: Int,
    val notification: Notification,
)