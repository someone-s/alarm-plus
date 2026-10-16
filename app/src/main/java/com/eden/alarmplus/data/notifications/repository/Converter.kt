package com.eden.alarmplus.data.notifications.repository

import androidx.core.net.toUri
import com.eden.alarmplus.data.common.repository.toUuid
import com.eden.alarmplus.data.common.repository.toUuidLongs
import com.eden.alarmplus.data.notifications.database.AlertEntity
import com.eden.alarmplus.data.notifications.database.ForewarnEntity
import com.eden.alarmplus.data.notifications.structure.AlertState
import com.eden.alarmplus.data.notifications.structure.ForewarnState
import java.time.LocalTime

fun AlertState.toAlertEntity() = AlertEntity(
    groupUuid = this.groupUuid.toUuidLongs(),
    notificationId = this.notificationId,
    displayedName = this.displayedName,
    displayedTimestamp = this.displayedTime?.let { it.hour * 60 + it.minute },
    snoozeAllowed = this.snoozeAllowed,
    dismissAllowed = this.dismissAllowed,
    audioUriString = this.audioUri?.toString(),
    vibrateEnabled = this.vibrateEnabled,
)

fun AlertEntity.toAlertState() = AlertState(
    groupUuid = this.groupUuid.toUuid(),
    notificationId = this.notificationId,
    displayedName = this.displayedName,
    displayedTime = this.displayedTimestamp?.let { LocalTime.of(it / 60, it % 60) },
    snoozeAllowed = this.snoozeAllowed,
    dismissAllowed = this.dismissAllowed,
    audioUri = this.audioUriString?.toUri(),
    vibrateEnabled = this.vibrateEnabled,
)

fun ForewarnState.toForewarnEntity() = ForewarnEntity(
    groupUuid = this.groupUuid.toUuidLongs(),
    notificationId = this.notificationId,
)

fun ForewarnEntity.toForewarnState() = ForewarnState(
    groupUuid = this.groupUuid.toUuid(),
    notificationId = this.notificationId,
)