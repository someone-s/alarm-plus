package com.eden.alarmplus.alert

import android.app.Notification
import com.eden.alarmplus.data.alarms.structure.audio.AudioData
import kotlin.uuid.Uuid

data class AlertState(
    val groupUuid: Uuid,
    val notificationId: Int,
    val notification: Notification,
    val audioData: AudioData?,
    val vibrateEnabled: Boolean,
) {
}