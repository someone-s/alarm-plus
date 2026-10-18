package com.eden.alarmplus.ui.main.alarms

import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmInfo
import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.group.feature.DayOfWeekFeature
import com.eden.alarmplus.data.alarms.structure.state.StateData
import com.eden.alarmplus.data.datetime.repository.TimeTickRepository
import com.eden.alarmplus.logic.access.StateHookProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.uuid.Uuid

val mockGroupAlarmViewModel = GroupAlarmViewModel(
    groupAlarmsRepository = object : GroupAlarmsRepository {
        override suspend fun notify(notification: GroupAlarmsRepository.ChangeNotification) {}

        override fun getAllGroupsStream():Flow<List<Pair<GroupData, StateData>>> = flowOf(listOf(
            Pair(
                GroupData(
                    info = GroupInfo(
                        name = "Morning alarms",
                        enabled = true,
                        features = listOf(
                            DayOfWeekFeature(DayOfWeekFlags.merge(
                                DayOfWeekFlags.MONDAY,
                                DayOfWeekFlags.TUESDAY,
                                DayOfWeekFlags.WEDNESDAY
                            ))
                        ),
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
                    ),
                ),
                StateData(Uuid.random(), StateData.Type.ACTIVE)
            ),
            Pair(
                GroupData(
                    info = GroupInfo(
                        name = "Lunch alerts",
                        enabled = false,
                        features = emptyList(),
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
                    ),
                ),
                StateData(Uuid.random(), StateData.Type.IDLE)
            ),
        ))
    },
    timeTickRepository = object : TimeTickRepository {
        override fun currentDateTime(): Flow<ZonedDateTime> {
            return flowOf(ZonedDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneId.systemDefault()))
        }
    },
    stateHookProvider = object : StateHookProvider {
        override fun initializeState(dateTime: LocalDateTime) {}

        override fun dismissGroup(groupUuid: Uuid) {}
    }
)