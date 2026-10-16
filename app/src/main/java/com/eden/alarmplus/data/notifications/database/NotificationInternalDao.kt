package com.eden.alarmplus.data.notifications.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction

@Dao
interface NotificationInternalDao: NotificationDao {

    @Transaction
    @Query(
        """
            DELETE
            FROM alerts
        """
    )
    suspend fun resetAlerts()

    @Transaction
    @Query(
        """
            DELETE
            FROM forewarns
        """
    )
    suspend fun resetForewarns()

    @Transaction
    override suspend fun reset() {
        resetAlerts()
        resetForewarns()
    }

    @Query(
        """
            SELECT COUNT(*)
            FROM alerts
        """
    )
    override suspend fun getAlertCount(): Int

    @Query(
        """
            SELECT *
            FROM alerts
            ORDER BY `index` DESC
            LIMIT 1
        """
    )
    override suspend fun getLastAlert(): AlertEntity?

    /**
     * [AlertEntity.index] is ignored
     */
    @Transaction
    override suspend fun addLastAlert(alert: AlertEntity) {
        val newLastIndex = (getLastAlert()?.index?.plus(1)) ?: 0
        insertAlert(alert.shallowCopy(newLastIndex))
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAlert(alert: AlertEntity)

    @Query(
        """
            SELECT *
            FROM alerts
        """
    )
    override suspend fun getAlerts(): List<AlertEntity>

    @Query(
        """
            SELECT `index`
            FROM alerts
            WHERE
                group_upper_uuid = :groupUpperUuid
                AND
                group_lower_uuid = :groupLowerUuid
        """
    )
    suspend fun getIndex(groupUpperUuid: Long, groupLowerUuid: Long): Int?

    @Transaction
    @Query(
        """
            DELETE
            FROM alerts
            WHERE `index` = :index
        """
    )
    suspend fun deleteAlertAtIndex(index: Int)

    @Transaction
    @Query(
        """
            UPDATE alerts
            SET `index` = `index` - 1
            WHERE `index` > :index
        """
    )
    suspend fun shiftAlertIndexAfter(index: Int)

    @Transaction
    override suspend fun deleteAlert(groupUpperUuid: Long, groupLowerUuid: Long) {
        val index = getIndex(groupUpperUuid, groupLowerUuid)
        if (index != null) {
            deleteAlertAtIndex(index)
            shiftAlertIndexAfter(index)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun setForewarn(forewarn: ForewarnEntity)

    @Query(
        """
            SELECT *
            FROM forewarns
            WHERE
                group_upper_uuid = :groupUpperUuid
                AND
                group_lower_uuid = :groupLowerUuid
        """
    )
    override suspend fun getForewarn(groupUpperUuid: Long, groupLowerUuid: Long): ForewarnEntity?

    @Query(
        """
            SELECT *
            FROM forewarns
        """
    )
    override suspend fun getForewarns(): List<ForewarnEntity>
    @Transaction
    @Query(
        """
            DELETE
            FROM forewarns
            WHERE
                group_upper_uuid = :groupUpperUuid
                AND
                group_lower_uuid = :groupLowerUuid
        """
    )
    override suspend fun deleteForewarn(groupUpperUuid: Long, groupLowerUuid: Long)
}
