package com.eden.alarmplus.data.alarms.database

import kotlinx.coroutines.flow.Flow

interface GroupsWithAlarmsDao {
    suspend fun insertOrReplace(group: GroupEntity, alarms: List<AlarmEntity>)

    suspend fun toggle(groupUpperUuid: Long, groupLowerUuid: Long, active: Boolean)

    suspend fun delete(groupUpperUuid: Long, groupLowerUuid: Long)

    fun getAll(): Flow<List<GroupWithAlarms>>
}

