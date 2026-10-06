package com.eden.alarmplus.ui.main.alarms

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.state.StateData
import com.eden.alarmplus.data.datetime.repository.TimeTickRepository
import com.eden.alarmplus.logic.access.StateHookProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.uuid.Uuid

class GroupAlarmViewModel(
    private val groupAlarmsRepository: GroupAlarmsRepository,
    timeTickRepository: TimeTickRepository,
    private val stateHookProvider: StateHookProvider,
): ViewModel() {

    val currentTimeUiState: StateFlow<CurrentTimeUiState> =
        timeTickRepository.currentDateTime()
            .map { CurrentTimeUiState(it.toLocalDateTime(), it.toLocalTime()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = CurrentTimeUiState(),
            )

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

    suspend fun toggleGroupAlarm(groupUuid: Uuid, enabled: Boolean) {
        groupAlarmsRepository.toggle(groupUuid, enabled)
    }

    suspend fun dismissGroupAlarm(groupUuid: Uuid) {
        stateHookProvider.dismissGroup(groupUuid)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

@Immutable
data class GroupAlarmUiState(val groupAndStateList: List<Pair<GroupData, StateData>> = listOf())

@Immutable
data class CurrentTimeUiState(val localDateTime: LocalDateTime = LocalDateTime.now(), val localTime: LocalTime = LocalTime.now())