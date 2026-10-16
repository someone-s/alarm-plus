package com.eden.alarmplus.data.notifications.repository

import com.eden.alarmplus.data.common.repository.toUuidLongs
import com.eden.alarmplus.data.notifications.database.NotificationDao
import com.eden.alarmplus.data.notifications.structure.AlertState
import com.eden.alarmplus.data.notifications.structure.ForewarnState
import kotlin.uuid.Uuid

class OfflineNotificationRepository(private val notificationDao: NotificationDao): NotificationRepository {

    override suspend fun reset() {
        notificationDao.reset()
    }

    override suspend fun hasAnyAlerts(): Boolean {
        return notificationDao.getAlertCount() > 0
    }

    override suspend fun addLastAlert(alert: AlertState) {
        return notificationDao.addLastAlert(alert.toAlertEntity())
    }

    override suspend fun getLastAlert(): AlertState? {
        return notificationDao.getLastAlert()?.toAlertState()
    }

    override suspend fun getAlerts(): List<AlertState> {
        return notificationDao.getAlerts().map { it.toAlertState() }
    }

    override suspend fun deleteAlert(groupUuid: Uuid) {
        val longs = groupUuid.toUuidLongs()
        notificationDao.deleteAlert(longs.upperUuid, longs.lowerUuid)
    }

    override suspend fun setForewarn(forewarn: ForewarnState) {
        notificationDao.setForewarn(forewarn.toForewarnEntity())
    }

    override suspend fun getForewarn(groupUuid: Uuid): ForewarnState? {
        val longs = groupUuid.toUuidLongs()
        return notificationDao.getForewarn(longs.upperUuid, longs.lowerUuid)?.toForewarnState()
    }

    override suspend fun getForewarns(): List<ForewarnState> {
        return notificationDao.getForewarns().map { it.toForewarnState() }
    }

    override suspend fun deleteForewarn(groupUuid: Uuid) {
        val longs = groupUuid.toUuidLongs()
        notificationDao.deleteForewarn(longs.upperUuid, longs.lowerUuid)
    }
}