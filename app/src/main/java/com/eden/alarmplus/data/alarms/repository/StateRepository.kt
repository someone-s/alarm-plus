package com.eden.alarmplus.data.alarms.repository

import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.state.StateData
import kotlin.uuid.Uuid

interface StateRepository {

    suspend fun getAllGroups(): List<GroupData>

    suspend fun getState(groupUuid: Uuid): StateData?

    suspend fun updateState(state: StateData)
}