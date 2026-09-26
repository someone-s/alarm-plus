package com.eden.alarmplus.ui.main.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eden.alarmplus.data.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.structure.group.GroupData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.uuid.Uuid

class GroupAlarmViewModel(private val groupAlarmsRepository: GroupAlarmsRepository): ViewModel() {
    val groupAlarmsUiState: StateFlow<GroupAlarmUiState> =
        groupAlarmsRepository.getAllGroupsStream()
            .map { GroupAlarmUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = GroupAlarmUiState(),
            )

    suspend fun insertOrReplaceGroupAlarm(group: GroupData) {
        groupAlarmsRepository.insertOrReplace(group)
    }

    suspend fun deleteGroupAlarm(groupUuid: Uuid) {
        groupAlarmsRepository.delete(groupUuid)
    }

    suspend fun toggleGroupAlarm(groupUuid: Uuid, active: Boolean) {
        groupAlarmsRepository.toggle(groupUuid, active)
    }


    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class GroupAlarmUiState(val groupDataList: List<GroupData> = listOf())