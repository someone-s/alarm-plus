package com.eden.alarmplus.data.alarms.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update

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
    suspend fun getAllGroups(): List<GroupWithAll>

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