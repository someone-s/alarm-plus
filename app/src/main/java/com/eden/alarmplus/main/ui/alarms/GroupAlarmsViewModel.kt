package com.eden.alarmplus.main.ui.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eden.alarmplus.data.repository.GroupAlarmsRepository
import com.eden.alarmplus.structure.group.GroupData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class GroupAlarmViewModel(private val groupAlarmsRepository: GroupAlarmsRepository): ViewModel() {
    val groupAlarmsUiState: StateFlow<GroupAlarmUiState> =
        groupAlarmsRepository.getAllGroupAlarmsStream()
            .map { GroupAlarmUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = GroupAlarmUiState(),
            )

    private val _groupAlarmsChange = MutableSharedFlow<GroupAlarmsChange>()
    val groupAlarmsChange: SharedFlow<GroupAlarmsChange> = _groupAlarmsChange

    suspend fun insertOrReplaceGroupAlarm(group: GroupData) {
        groupAlarmsRepository.insertOrReplace(group)
        viewModelScope.launch {
            _groupAlarmsChange.emit(GroupAlarmsChange.Update(group.info.uuid))
        }
    }

    suspend fun deleteGroupAlarm(groupUuid: Uuid) {
        groupAlarmsRepository.delete(groupUuid)
        viewModelScope.launch {
            _groupAlarmsChange.emit(GroupAlarmsChange.Delete())
        }
    }

    suspend fun toggleGroupAlarm(groupUuid: Uuid, active: Boolean) {
        groupAlarmsRepository.toggle(groupUuid, active)
        viewModelScope.launch {
            _groupAlarmsChange.emit(GroupAlarmsChange.Update(groupUuid))
        }
    }


    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class GroupAlarmUiState(val groupDataList: List<GroupData> = listOf())

sealed class GroupAlarmsChange {
    data class Update(val groupUuid: Uuid): GroupAlarmsChange()
    class Delete: GroupAlarmsChange()
}