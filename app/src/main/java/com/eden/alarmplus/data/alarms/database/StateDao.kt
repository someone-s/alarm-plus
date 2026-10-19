package com.eden.alarmplus.data.alarms.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction

@Dao
interface StateDao {

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
    suspend fun getAllGroups(): List<GroupWithAlarms>

    @Query(
        """
            SELECT state
            FROM groups
            WHERE
                group_upper_uuid = :groupUpperUuid
                AND
                group_lower_uuid = :groupLowerUuid
        """
    )
    suspend fun getState(groupUpperUuid: Long, groupLowerUuid: Long): Int?

    /**
     * @param state defined by [GroupEntity.Companion]
     */
    @Transaction
    @Query(
        """
            UPDATE groups
            SET 
                state = :state
            WHERE
                group_upper_uuid = :groupUpperUuid
                AND
                group_lower_uuid = :groupLowerUuid
        """
    )
    suspend fun updateState(groupUpperUuid: Long, groupLowerUuid: Long, state: Int)
}