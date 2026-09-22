package com.eden.alarmplus.data.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update

@Dao
interface StateDao {
    /**
     * @param timestamp in whole minutes
     * @return next alarm's timestamp in whole minutes, null if no alarms
     */
    @Query(
        """
            WITH 
                after_timestamp_cte AS (
                    SELECT alarms.timestamp 
                    FROM alarms
                    LEFT JOIN groups ON 
                        groups.group_upper_uuid = alarms.group_upper_uuid
                        AND
                        groups.group_lower_uuid = alarms.group_lower_uuid
                    WHERE 
                        groups.active
                        AND
                        alarms.timestamp > :timestamp
                    ORDER BY timestamp ASC
                    LIMIT 1
                ),
                first_timestamp_cte AS (
                    SELECT alarms.timestamp
                    FROM alarms
                    LEFT JOIN groups ON 
                        groups.group_upper_uuid = alarms.group_upper_uuid
                        AND
                        groups.group_lower_uuid = alarms.group_lower_uuid
                    WHERE groups.active
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
    suspend fun findNextActiveTimestampAfter(timestamp: Int): Int?

    /**
     * [Transaction] annotation required as using relations.
     */
    @Transaction
    @Query(
        """
            SELECT *
            FROM groups
        """
    )
    suspend fun getAllGroupAlarms(): List<GroupWithAlarms>
    
    @Query(
        """
            SELECT *
            FROM states
            WHERE
                group_upper_uuid = :groupUpperUuid
                AND
                group_lower_uuid = :groupLowerUuid
        """
    )
    suspend fun getState(groupUpperUuid: Long, groupLowerUuid: Long): StateEntity?

    /**
     * Note as per update's guarantee, if primary key does not exist, no changes are made
     */
    @Update
    suspend fun updateState(state: StateEntity)
}