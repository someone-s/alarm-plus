package com.eden.alarmplus.data.alarms.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupsInternalDao: GroupsDao {
    /**
     * Combined transaction to prevent race conditions.
     * [androidx.room3.Transaction] annotation added to ensure underlying action are run together or rolled back together.
     * @param group to add
     * @param alarms belonging to the group
     */
    @Transaction
    override suspend fun insertOrReplace(group: GroupEntity, alarms: List<AlarmEntity>) {
        delete(group.uuid.upperUuid, group.uuid.lowerUuid)
        internalInsertFull(group, alarms)
    }

    /**
     * Do not use under any circumstance.
     * [Transaction] is already inherit-ly implemented with [androidx.room3.Insert]
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalInsertFull(group: GroupEntity, alarms: List<AlarmEntity>)

    /**
     * [Transaction] annotation added to reiterate this is a write operation.
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
    suspend fun internalSetState(groupUpperUuid: Long, groupLowerUuid: Long, state: Int)

    @Transaction
    override suspend fun toggleResetState(groupUpperUuid: Long, groupLowerUuid: Long, enabled: Boolean) {
        internalSetState(
            groupUpperUuid,
            groupLowerUuid,
            if (enabled) GroupEntity.ENABLED_UNDEFINED_MANUAL_VALUE else GroupEntity.DISABLED_VALUE
        )
    }

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
    override suspend fun delete(groupUpperUuid: Long, groupLowerUuid: Long)

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
    override fun getAll(): Flow<List<GroupWithAlarms>>
}