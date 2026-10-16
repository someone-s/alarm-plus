package com.eden.alarmplus.data.notifications.structure

import android.net.Uri
import java.time.LocalTime
import kotlin.uuid.Uuid

data class AlertState(
    val groupUuid: Uuid,
    val notificationId: Int,
    val displayedName: String?,
    val displayedTime: LocalTime?,
    val snoozeAllowed: Boolean,
    val dismissAllowed: Boolean,
    val audioUri: Uri?,
    val vibrateEnabled: Boolean,
)