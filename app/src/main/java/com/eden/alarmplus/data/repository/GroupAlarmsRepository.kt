package com.eden.alarmplus.data.repository

import com.eden.alarmplus.structure.group.GroupData
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime
import kotlin.uuid.Uuid

interface GroupAlarmsRepository {
    suspend fun insert(group: GroupData)

    suspend fun toggle(groupUuid: Uuid, active: Boolean)

    suspend fun delete(groupUuid: Uuid)

    suspend fun insertOrReplace(group: GroupData)

    suspend fun getNextAlarmTimeAfter(time: LocalTime): LocalTime

    fun getAllGroupAlarmsStream(): Flow<List<GroupData>>
}