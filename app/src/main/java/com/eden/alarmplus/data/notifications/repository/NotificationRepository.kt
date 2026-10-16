package com.eden.alarmplus.data.notifications.repository

import com.eden.alarmplus.data.notifications.structure.AlertState
import com.eden.alarmplus.data.notifications.structure.ForewarnState
import kotlin.uuid.Uuid

interface NotificationRepository {

    suspend fun reset()

    suspend fun hasAnyAlerts(): Boolean
    suspend fun addLastAlert(alert: AlertState)
    suspend fun getLastAlert(): AlertState?
    suspend fun getAlerts(): List<AlertState>
    suspend fun deleteAlert(groupUuid: Uuid)

    suspend fun setForewarn(forewarn: ForewarnState)
    suspend fun getForewarn(groupUuid: Uuid): ForewarnState?
    suspend fun getForewarns(): List<ForewarnState>
    suspend fun deleteForewarn(groupUuid: Uuid)
}