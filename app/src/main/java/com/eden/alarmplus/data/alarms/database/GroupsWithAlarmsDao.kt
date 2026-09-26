package com.eden.alarmplus.data.alarms.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupsWithAlarmsDao {
    /**
     * Combined transaction to prevent race conditions.
     * [Transaction] annotation added to ensure underlying action are run together or rolled back together.
     * @param group to add
     * @param alarms belonging to the group
     */
    @Transaction
    suspend fun insertOrReplace(group: GroupEntity, alarms: List<AlarmEntity>) {
        delete(group.uuid.upperUuid, group.uuid.lowerUuid)
        internalInsert(group, alarms, StateEntity(group.uuid))
    }

    /**
     * Do not use under any circumstance.
     * [Transaction] is already inherit-ly implemented with [Insert]
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalInsert(group: GroupEntity, alarms: List<AlarmEntity>, state: StateEntity)

    /**
     * [Transaction] annotation added to reiterate this is a write operation.
     */
    @Transaction
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
     * GroupEntity(s) with matching Uuid are deleted.
     * [Transaction] annotation added to reiterate this is a write operation.
     */
    @Transaction
    @Query(
        """
            DELETE FROM groups
            WHERE
                group_upper_uuid = :groupUpperUuid
                AND
                group_lower_uuid = :groupLowerUuid
        """
    )
    suspend fun delete(groupUpperUuid: Long, groupLowerUuid: Long)

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
    fun getAll(): Flow<List<GroupWithAlarms>>
}