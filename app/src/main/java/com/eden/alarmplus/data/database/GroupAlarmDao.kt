package com.eden.alarmplus.data.database

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupAlarmDao {
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

    @Transaction
    @Query(
        """
            SELECT * 
            FROM groups 
            WHERE
                group_upper_uuid = :groupUpperUuid
                AND
                group_lower_uuid = :groupLowerUuid
        """
    )
    fun get(groupUpperUuid: Long, groupLowerUuid: Long): Flow<GroupWithAlarms>

    @Transaction
    @Query(
        """
            SELECT *
            FROM groups
        """
    )
    fun getAll(): Flow<List<GroupWithAlarms>>
}