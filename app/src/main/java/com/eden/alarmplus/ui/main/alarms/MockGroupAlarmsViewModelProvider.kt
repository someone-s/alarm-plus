package com.eden.alarmplus.ui.main.alarms

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmInfo
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.state.StateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalTime
import kotlin.uuid.Uuid

object MockGroupAlarmViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            GroupAlarmViewModel(
                groupAlarmsRepository = object : GroupAlarmsRepository {
                    override suspend fun notify(notification: GroupAlarmsRepository.ChangeNotification) {}

                    override fun getAllGroupsStream():Flow<List<Pair<GroupData, StateData>>> = flowOf(listOf(
                        Pair(
                            GroupData(
                                info = GroupInfo(
                                    name = "Morning alarms",
                                    active = true,
                                ),
                                alarms = listOf(
                                    AlarmData(
                                        info = AlarmInfo(
                                            time = LocalTime.of(8, 30),
                                        ),
                                        features = emptyList(),
                                    ),
                                    AlarmData(
                                        info = AlarmInfo(
                                            time = LocalTime.of(9, 30),
                                        ),
                                        features = emptyList(),
                                    ),
                                    AlarmData(
                                        info = AlarmInfo(
                                            time = LocalTime.of(10, 30),
                                        ),
                                        features = emptyList(),
                                    ),
                                )
                            ),
                            StateData.Active(Uuid.random())
                        ),
                        Pair(
                            GroupData(
                                info = GroupInfo(
                                    name = "Lunch alerts",
                                    active = false,
                                ),
                                alarms = listOf(
                                    AlarmData(
                                        info = AlarmInfo(
                                            time = LocalTime.of(12, 30),
                                        ),
                                        features = emptyList(),
                                    ),
                                    AlarmData(
                                        info = AlarmInfo(
                                            time = LocalTime.of(13, 30),
                                        ),
                                        features = emptyList(),
                                    ),
                                )
                            ),
                            StateData.Idle(Uuid.random())
                        ),
                    ))
                },
                timeProvider = object : GroupAlarmViewModel.TimeProvider {
                    override val currentTime: LocalTime
                        get() = LocalTime.of(9, 0)

                }
            )
        }
    }
}
