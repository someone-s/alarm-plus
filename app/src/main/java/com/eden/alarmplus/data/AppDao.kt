package com.eden.alarmplus.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.eden.alarmplus.structure.group.GroupData
import kotlin.uuid.Uuid

@Dao
interface AppDao {
    suspend fun insert(group: GroupData) {
        internalInsert(group.info.toGroupEntity(), group.alarms.map { it.toAlarmEntity(group.info.uuid) })
    }

    suspend fun toggle(groupUuid: Uuid, active: Boolean) {
        val longs = groupUuid.toUuidLongs()
        internalToggle(longs.upperUuid, longs.lowerUuid, active)
    }
    suspend fun delete(groupUuid: Uuid) {
        internalDelete(GroupEntity(
            uuid = groupUuid.toUuidLongs(),
            name = null,
            active = false,
        ))
    }

    suspend fun replace(group: GroupData) {
        delete(group.info.uuid)
        insert(group)
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalInsert(group: GroupEntity, alarms: List<AlarmEntity>)

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
    suspend fun internalToggle(groupUpperUuid: Long, groupLowerUuid: Long, active: Boolean)

    /**
     * Room only compares by primary key;
     * GroupEntity(s) with matching Uuid are deleted
     */
    @Delete
    suspend fun internalDelete(group: GroupEntity)
}