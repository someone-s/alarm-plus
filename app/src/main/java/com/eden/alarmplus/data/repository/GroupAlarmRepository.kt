package com.eden.alarmplus.data.repository

import com.eden.alarmplus.structure.group.GroupData
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface GroupAlarmRepository {
    suspend fun insert(group: GroupData)

    suspend fun toggle(groupUuid: Uuid, active: Boolean)

    suspend fun delete(groupUuid: Uuid)

    suspend fun insertOrReplace(group: GroupData)

    fun getGroupAlarmStream(groupUuid: Uuid): Flow<GroupData>

    fun getAllGroupAlarmsStream(): Flow<List<GroupData>>
}