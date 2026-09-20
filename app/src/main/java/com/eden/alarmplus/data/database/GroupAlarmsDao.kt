package com.eden.alarmplus.data.database

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupAlarmsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: GroupEntity, alarms: List<AlarmEntity>)

    @Query(
        """
            UPDATE groups
            SET 
                active = :active
            WHERE
                group_upper_uuid = :groupUpperUuid
                AND
                group_lower_uuid = :groupLowerUuid
        """
    )
    suspend fun toggle(groupUpperUuid: Long, groupLowerUuid: Long, active: Boolean)

    /**
     * Room only compares by primary key;
     * GroupEntity(s) with matching Uuid are deleted
     */
    @Delete
    suspend fun delete(group: GroupEntity)

    /**
     * @param timestamp in whole minutes
     * @return next alarm's timestamp in whole minutes
     */
    @Query(
        """
            WITH 
                after_timestamp_cte AS (
                    SELECT timestamp 
                    FROM alarms
                    WHERE timestamp > :timestamp
                    ORDER BY timestamp ASC
                    LIMIT 1
                ),
                first_timestamp_cte AS (
                    SELECT timestamp
                    FROM alarms
                    ORDER BY timestamp ASC
                    LIMIT 1
                )
            SELECT timestamp FROM after_timestamp_cte
            UNION
            SELECT timestamp FROM first_timestamp_cte
            ORDER BY timestamp DESC
            LIMIT 1
            
        """
    )
    suspend fun getTimestampAfter(timestamp: Int): Int

    @Transaction
    @Query(
        """
            SELECT *
            FROM groups
        """
    )
    fun getAll(): Flow<List<GroupWithAlarms>>
}