package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.state.StateType
import kotlin.uuid.Uuid

interface StateRepository {

    suspend fun getAllGroups(): List<GroupData>

    suspend fun getState(groupUuid: Uuid): StateType?

    suspend fun updateState(groupUuid: Uuid, state: StateType)
}