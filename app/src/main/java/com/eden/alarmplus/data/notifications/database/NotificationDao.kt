package com.eden.alarmplus.data.notifications.database

interface NotificationDao {
    suspend fun reset()
    suspend fun getAlertCount(): Int

    suspend fun getLastAlert(): AlertEntity?

    /**
     * [AlertEntity.index] is ignored
     */
    suspend fun addLastAlert(alert: AlertEntity)

    suspend fun getAlerts(): List<AlertEntity>

    suspend fun deleteAlert(groupUpperUuid: Long, groupLowerUuid: Long)

    suspend fun setForewarn(forewarn: ForewarnEntity)

    suspend fun getForewarn(groupUpperUuid: Long, groupLowerUuid: Long): ForewarnEntity?

    suspend fun getForewarns(): List<ForewarnEntity>

    suspend fun deleteForewarn(groupUpperUuid: Long, groupLowerUuid: Long)
}