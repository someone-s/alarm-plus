package com.eden.alarmplus.ui.main.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.state.StateData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalTime
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class GroupAlarmViewModel(
    private val groupAlarmsRepository: GroupAlarmsRepository,
    private val timeProvider: TimeProvider = object : TimeProvider {
        override val currentTime
            get() = LocalTime.now()
    }
): ViewModel() {

    interface TimeProvider {
        val currentTime: LocalTime
    }

    val currentTimeUiState: StateFlow<LocalTime> = flow {
        while (true) {
            emit(timeProvider.currentTime)
            delay(1.minutes)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = timeProvider.currentTime
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

    suspend fun toggleGroupAlarm(groupUuid: Uuid, active: Boolean) {
        groupAlarmsRepository.toggle(groupUuid, active)
    }


    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class GroupAlarmUiState(val groupAndStateList: List<Pair<GroupData, StateData>> = listOf())