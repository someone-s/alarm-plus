package com.eden.alarmplus.main.ui.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eden.alarmplus.data.repository.GroupAlarmRepository
import com.eden.alarmplus.structure.group.GroupData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.uuid.Uuid

class GroupAlarmViewModel(private val groupAlarmRepository: GroupAlarmRepository): ViewModel() {
    val groupAlarmUiState: StateFlow<GroupAlarmUiState> =
        groupAlarmRepository.getAllGroupAlarmsStream()
            .map { GroupAlarmUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = GroupAlarmUiState(),
            )

    suspend fun insertOrReplaceGroupAlarm(group: GroupData) {
        groupAlarmRepository.insertOrReplace(group)
    }

    suspend fun deleteGroupAlarm(groupUuid: Uuid) {
        groupAlarmRepository.delete(groupUuid)
    }

    suspend fun toggleGroupAlarm(groupUuid: Uuid, active: Boolean) {
        groupAlarmRepository.toggle(groupUuid, active)
    }


    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class GroupAlarmUiState(val groupDataList: List<GroupData> = listOf())