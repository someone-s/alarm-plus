package com.eden.alarmplus.data.alarms.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update

@Dao
interface StateDao {
    /**
     * @param timestamp in whole minutes
     * @return next alarm or dismissable window start timestamp in whole minutes, null if no alarms
     */
    @Query(
        """
            WITH 
                after_alarm_timestamp_cte AS (
                    SELECT alarms.timestamp 
                    FROM alarms
                    LEFT JOIN groups ON 
                        groups.group_upper_uuid = alarms.group_upper_uuid
                        AND
                        groups.group_lower_uuid = alarms.group_lower_uuid
                    WHERE 
                        groups.enabled
                        AND
                        alarms.timestamp > :timestamp
                    ORDER BY timestamp ASC
                    LIMIT 1
                ),
                first_alarm_timestamp_cte AS (
                    SELECT alarms.timestamp
                    FROM alarms
                    LEFT JOIN groups ON 
                        groups.group_upper_uuid = alarms.group_upper_uuid
                        AND
                        groups.group_lower_uuid = alarms.group_lower_uuid
                    WHERE groups.enabled
                    ORDER BY timestamp ASC
                    LIMIT 1
                ),
                after_dismissable_timestamp_cte AS (
                    SELECT ((alarms.timestamp - :window + 60 * 24) % (60 * 24)) AS timestamp
                    FROM alarms
                    LEFT JOIN groups ON 
                        groups.group_upper_uuid = alarms.group_upper_uuid
                        AND
                        groups.group_lower_uuid = alarms.group_lower_uuid
                    WHERE 
                        groups.enabled
                        AND
                        ((alarms.timestamp - :window + 60 * 24) % (60 * 24)) > :timestamp
                    ORDER BY ((alarms.timestamp - :window + 60 * 24) % (60 * 24)) ASC
                    LIMIT 1
                ),
                first_dismissable_timestamp_cte AS (
                    SELECT ((alarms.timestamp - :window + 60 * 24) % (60 * 24)) AS timestamp
                    FROM alarms
                    LEFT JOIN groups ON 
                        groups.group_upper_uuid = alarms.group_upper_uuid
                        AND
                        groups.group_lower_uuid = alarms.group_lower_uuid
                    WHERE groups.enabled
                    ORDER BY ((alarms.timestamp - :window + 60 * 24) % (60 * 24)) ASC
                    LIMIT 1
                ),
                after_timestamp_cte AS (
                    SELECT timestamp FROM after_alarm_timestamp_cte
                    UNION
                    SELECT timestamp FROM after_dismissable_timestamp_cte
                    ORDER BY timestamp ASC
                    LIMIT 1
                ),
                first_timestamp_cte AS (
                    SELECT timestamp FROM first_alarm_timestamp_cte
                    UNION
                    SELECT timestamp FROM first_dismissable_timestamp_cte
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
    suspend fun findNextTimestampAfter(timestamp: Int, window: Int): Int?

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
                                ORDER BY sort_value DESC
                            )
                            AS alarm_sequence
                        FROM alarms
                    )
                    WHERE alarm_sequence = 1
                ),
                first_alarms_cte AS (
                    SELECT
                        group_upper_uuid,
                        group_lower_uuid,
                        timestamp
                    FROM (
                        SELECT
                            *,
                            ROW_NUMBER() OVER(
                                PARTITION BY 
                                    group_upper_uuid,
                                    group_lower_uuid
                                ORDER BY sort_value ASC
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
                AS is_last_in_group,
                CASE 
                    WHEN EXISTS (
                        SELECT *
                        FROM first_alarms_cte
                        WHERE
                            alarms.group_upper_uuid = first_alarms_cte.group_upper_uuid
                            AND alarms.group_lower_uuid = first_alarms_cte.group_lower_uuid
                            AND (
                                :timestamp >= (first_alarms_cte.timestamp - :window)
                                AND :timestamp < first_alarms_cte.timestamp
                            ) OR (
                                :timestamp - (24 * 60) >= (first_alarms_cte.timestamp - :window)
                                AND :timestamp - (24 * 60) < first_alarms_cte.timestamp
                            )
                    )
                    THEN true
                    ELSE false
                END
                AS in_dismissable_window
            FROM alarms
            WHERE 
                EXISTS (
                    SELECT *
                    FROM `groups`
                    WHERE
                        `groups`.group_upper_uuid = alarms.group_upper_uuid
                        AND
                        `groups`.group_lower_uuid = alarms.group_lower_uuid
                        AND
                        `groups`.enabled
                )
                AND
                alarms.timestamp = :timestamp
        """
    )
    suspend fun getAlarmsWithGroupsAtTimestamp(timestamp: Int, window: Int): List<AlarmAtTimestamp>

    /**
     * @param timestamp in minutes
     * @param window in minutes, how much time in advance does the window begin
     */
    @Transaction
    @Query(
        """
            WITH
                first_alarms_cte AS (
                    SELECT 
                        group_upper_uuid,
                        group_lower_uuid,
                        timestamp
                    FROM (
                        SELECT
                            *,
                            ROW_NUMBER() OVER(
                                PARTITION BY 
                                    group_upper_uuid,
                                    group_lower_uuid
                                ORDER BY sort_value ASC
                            )
                            AS alarm_sequence 
                        FROM alarms
                    )
                    WHERE alarm_sequence = 1
                )
            SELECT  
                first_alarms_cte.group_upper_uuid,
                first_alarms_cte.group_lower_uuid
            FROM first_alarms_cte
            WHERE 
                EXISTS (
                    SELECT *
                    FROM `groups`
                    WHERE
                        `groups`.group_upper_uuid = first_alarms_cte.group_upper_uuid
                        AND
                        `groups`.group_lower_uuid = first_alarms_cte.group_lower_uuid
                        AND
                        `groups`.enabled
                )
                AND
                (first_alarms_cte.timestamp + 60 * 24 - :window) % (60 * 24) = :timestamp
        """
    )
    suspend fun getPrimedAtTimestamp(timestamp: Int, window: Int): List<PrimedAtTimestamp>
    
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