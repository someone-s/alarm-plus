package com.eden.alarmplus.data.alarms.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update

@Dao
interface StateDao {

    companion object {

        private const val DAY_BEFORE_CTE = """
            day_before_cte AS (
                SELECT CASE
                    WHEN :day << 1 > 64 THEN 1
                    ELSE :day << 1
                    END AS day_before
            )
        """

        private const val GROUP_START_TIMESTAMP_CTE = """
            group_start_timestamp_cte AS (
                SELECT
                    group_upper_uuid,
                    group_lower_uuid,
                    timestamp
                FROM alarms
                WHERE sort_value = 0
            )
        """

        /**
         * [DAY_BEFORE_CTE] must be included;
         * [GROUP_START_TIMESTAMP_CTE] must be included;
         * "group_upper_uuid" column must be included;
         * "group_lower_uuid" column must be included;
         * "timestamp" column must be included;
         * "day_of_weeks_mask" column must be included
         */
        private const val FILTER_ALARM_DAY_OF_WEEK = """
            (
                day_of_weeks_mask IS NULL
                OR (
                    day_of_weeks_mask IS NOT NULL
                    AND (
                        (
                            EXISTS (
                                SELECT * FROM day_before_cte
                                WHERE (day_of_weeks_mask | day_before) != 0
                            )
                            AND (day_of_weeks_mask | :day) != 0
                        ) OR (
                            EXISTS (
                                SELECT * FROM day_before_cte
                                WHERE (day_of_weeks_mask | day_before) = 0
                            )
                            AND (day_of_weeks_mask | :day) != 0
                            AND EXISTS (
                                SELECT * FROM group_start_timestamp_cte
                                WHERE 
                                    group_upper_uuid = group_start_timestamp_cte.group_upper_uuid
                                    AND group_lower_uuid = group_start_timestamp_cte.group_lower_uuid
                                    AND timestamp >= group_start_timestamp_cte.timestamp
                            )
                        ) OR (
                            EXISTS (
                                SELECT * FROM day_before_cte
                                WHERE (day_of_weeks_mask | day_before) != 0
                            )
                            AND (day_of_weeks_mask | :day) = 0
                            AND EXISTS (
                                SELECT * FROM group_start_timestamp_cte
                                WHERE 
                                    group_upper_uuid = group_start_timestamp_cte.group_upper_uuid
                                    AND group_lower_uuid = group_start_timestamp_cte.group_lower_uuid
                                    AND timestamp < group_start_timestamp_cte.timestamp
                            )
                        )
                    )
                )
            )
        """
    }

    /**
     * @param timestamp in whole minutes
     * @param window the dismiss window in minutes ahead of an alarm
     * @param day the integer mask of the day being queried,
     * mask value from [com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags]
     * @return next alarm or dismissable window start timestamp in whole minutes, null if no alarms
     */
    @Query(
        """
            WITH 
                $DAY_BEFORE_CTE,
                $GROUP_START_TIMESTAMP_CTE,
                enabled_alarm_timestamp_cte AS (
                    SELECT 
                        alarms.group_upper_uuid,
                        alarms.group_lower_uuid,
                        timestamp,
                        day_of_weeks.mask as day_of_weeks_mask
                    FROM alarms
                    LEFT JOIN groups ON 
                        groups.group_upper_uuid = alarms.group_upper_uuid
                        AND groups.group_lower_uuid = alarms.group_lower_uuid
                    LEFT JOIN day_of_weeks ON
                        day_of_weeks.group_upper_uuid = alarms.group_upper_uuid
                        AND day_of_weeks.group_lower_uuid = alarms.group_lower_uuid
                    WHERE groups.enabled
                ),
                day_of_week_enabled_timestamp_cte AS (
                    SELECT timestamp
                    FROM enabled_alarm_timestamp_cte
                    WHERE $FILTER_ALARM_DAY_OF_WEEK
                ),
                after_alarm_timestamp_cte AS (
                    SELECT timestamp 
                    FROM day_of_week_enabled_timestamp_cte
                    WHERE timestamp > :timestamp
                    ORDER BY timestamp ASC
                    LIMIT 1
                ),
                first_alarm_timestamp_cte AS (
                    SELECT timestamp
                    FROM day_of_week_enabled_timestamp_cte
                    ORDER BY timestamp ASC
                    LIMIT 1
                ),
                enabled_dismissable_timestamp_cte AS (
                    SELECT 
                        alarms.group_upper_uuid,
                        alarms.group_lower_uuid,
                        ((alarms.timestamp - :window + 60 * 24) % (60 * 24)) AS timestamp,
                        CASE 
                            WHEN EXISTS (
                                SELECT * 
                                FROM group_start_timestamp_cte
                                WHERE 
                                    group_start_timestamp_cte.group_upper_uuid = alarms.group_upper_uuid
                                    AND group_start_timestamp_cte.group_lower_uuid = alarms.group_lower_uuid
                                    AND ((alarms.timestamp - :window + 60 * 24) % (60 * 24)) > group_start_timestamp_cte.timestamp
                            )
                            THEN true
                            ELSE false
                        END AS is_wrap_around,
                        day_of_weeks.mask AS unadjusted_day_of_weeks_mask
                    FROM alarms
                    LEFT JOIN groups ON 
                        groups.group_upper_uuid = alarms.group_upper_uuid
                        AND groups.group_lower_uuid = alarms.group_lower_uuid
                    LEFT JOIN day_of_weeks ON
                        day_of_weeks.group_upper_uuid = alarms.group_upper_uuid
                        AND day_of_weeks.group_lower_uuid = alarms.group_lower_uuid
                    WHERE 
                        groups.enabled
                        AND alarms.sort_value = 0
                ),
                enabled_adjusted_dismissable_timestamp_cte AS (
                    SELECT
                        group_upper_uuid,
                        group_lower_uuid,
                        timestamp,
                        CASE is_wrap_around
                        WHEN true THEN 
                            CASE
                                WHEN unadjusted_day_of_weeks_mask >> 1 = 0 THEN 64
                                ELSE unadjusted_day_of_weeks_mask >> 1
                            END
                        WHEN false THEN 
                            unadjusted_day_of_weeks_mask
                        END AS day_of_weeks_mask
                    FROM enabled_dismissable_timestamp_cte
                ),
                enabled_adjusted_day_of_week_dismissable_timestamp_cte AS (
                    SELECT timestamp
                    FROM enabled_adjusted_dismissable_timestamp_cte
                    WHERE $FILTER_ALARM_DAY_OF_WEEK
                ),
                after_dismissable_timestamp_cte AS (
                    SELECT timestamp
                    FROM enabled_adjusted_day_of_week_dismissable_timestamp_cte
                    WHERE timestamp > :timestamp
                    ORDER BY timestamp ASC
                    LIMIT 1
                ),
                first_dismissable_timestamp_cte AS (
                    SELECT timestamp
                    FROM enabled_adjusted_day_of_week_dismissable_timestamp_cte
                    ORDER BY timestamp ASC
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
    suspend fun findNextTimestampAfter(day: Int, timestamp: Int, window: Int): Int?

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
    suspend fun getAllGroups(): List<GroupWithAll>

    /**
     * [Transaction] annotation required as using relations.
     * @param day the integer mask of the day being queried,
     * mask value from [com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags]
     */
    @Transaction
    @Query(
        """
            WITH
                $DAY_BEFORE_CTE,
                $GROUP_START_TIMESTAMP_CTE,
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
                AND EXISTS (
                    SELECT *
                    FROM (
                        SELECT
                            day_of_weeks.group_upper_uuid AS group_upper_uuid,
                            day_of_weeks.group_lower_uuid AS group_lower_uuid,
                            alarms.timestamp AS timestamp,
                            day_of_weeks.mask AS day_of_weeks_mask
                        FROM day_of_weeks 
                        WHERE
                            day_of_weeks.group_upper_uuid = alarms.group_upper_uuid
                            AND day_of_weeks.group_lower_uuid = alarms.group_lower_uuid
                    )
                    WHERE $FILTER_ALARM_DAY_OF_WEEK
                )
                AND alarms.timestamp = :timestamp
        """
    )
    suspend fun getAlarmsWithGroupsAtTimestamp(day: Int, timestamp: Int, window: Int): List<AlarmAtTimestamp>

    /**
     * @param day the integer mask of the day being queried,
     * mask value from [com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags]
     * @param timestamp in minutes
     * @param window in minutes, how much time in advance does the window begin
     */
    @Transaction
    @Query(
        """
            WITH
                $DAY_BEFORE_CTE,
                $GROUP_START_TIMESTAMP_CTE,
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
                AND EXISTS (
                    SELECT *
                    FROM (
                        SELECT
                            day_of_weeks.group_upper_uuid AS group_upper_uuid,
                            day_of_weeks.group_lower_uuid AS group_lower_uuid,
                            first_alarms_cte.timestamp AS timestamp,
                            day_of_weeks.mask AS day_of_weeks_mask
                        FROM day_of_weeks 
                        WHERE
                            day_of_weeks.group_upper_uuid = first_alarms_cte.group_upper_uuid
                            AND day_of_weeks.group_lower_uuid = first_alarms_cte.group_lower_uuid
                    )
                    WHERE $FILTER_ALARM_DAY_OF_WEEK
                )
                AND (first_alarms_cte.timestamp + 60 * 24 - :window) % (60 * 24) = :timestamp
        """
    )
    suspend fun getPrimedAtTimestamp(day: Int, timestamp: Int, window: Int): List<PrimedAtTimestamp>
    
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