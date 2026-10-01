package com.eden.alarmplus.alert

import android.app.Notification
import kotlin.uuid.Uuid

data class AlertState(
    val groupUuid: Uuid,
    val notificationId: Int,
    val notification: Notification,
    val isFullScreen: Boolean,
) {
    fun shallowCopy(isFullScreen: Boolean) = AlertState(
        groupUuid = this.groupUuid,
        notificationId = this.notificationId,
        notification = this.notification,
        isFullScreen = isFullScreen,
    )
}