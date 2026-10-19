package com.eden.alarmplus.data.alarms.database

import kotlinx.coroutines.flow.Flow

interface GroupsDao {
    suspend fun insertOrReplace(group: GroupEntity, alarms: List<AlarmEntity>)

    suspend fun toggleResetState(groupUpperUuid: Long, groupLowerUuid: Long, enabled: Boolean)

    suspend fun delete(groupUpperUuid: Long, groupLowerUuid: Long)

    fun getAll(): Flow<List<GroupWithAlarms>>
}

