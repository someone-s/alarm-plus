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
    suspend fun getAllGroupsWithAlarms(): List<GroupWithAlarms>

    /**
     * [Transaction] annotation required as using relations.
     */
    @Transaction
    @Query(
        """
            WITH
                last_alarms_cte AS (
                    SELECT
                        alarm_upper_uuid,
                        alarm_lower_uuid
                    FROM (
                        SELECT
                            *,
                            ROW_NUMBER() OVER(
                                PARTITION BY 
                                    group_upper_uuid,
                                    group_lower_uuid
                                ORDER BY timestamp DESC
                            )
                            AS alarm_sequence
                        FROM alarms
                    )
                    WHERE alarm_sequence = 1
                )
            SELECT 
                *, 
                CASE
                    WHEN EXISTS (
                        SELECT *
                        FROM last_alarms_cte
                        WHERE
                            alarms.alarm_upper_uuid = last_alarms_cte.alarm_upper_uuid
                            AND
                            alarms.alarm_lower_uuid = last_alarms_cte.alarm_lower_uuid
                    )
                    THEN true
                    ELSE false
                END
                AS is_last_in_group
            FROM alarms
            LEFT JOIN groups ON 
                groups.group_upper_uuid = alarms.group_upper_uuid
                AND
                groups.group_lower_uuid = alarms.group_lower_uuid
            WHERE 
                groups.active
                AND
                alarms.timestamp = :timestamp
        """
    )
    suspend fun getAlarmsWithGroupsAtTimestamp(timestamp: Int): List<AlarmAtTimestamp>
    
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