package com.eden.alarmplus.ui.main.alarms

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmInfo
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.state.StateData
import com.eden.alarmplus.data.datetime.repository.TimeTickRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
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
                            StateData(Uuid.random(), StateData.Type.ACTIVE)
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
                            StateData(Uuid.random(), StateData.Type.IDLE)
                        ),
                    ))
                },
                timeTickRepository = object : TimeTickRepository {
                    override fun currentDateTime(): Flow<ZonedDateTime> {
                        return flowOf(ZonedDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneId.systemDefault()))
                    }
                }
            )
        }
    }
}
