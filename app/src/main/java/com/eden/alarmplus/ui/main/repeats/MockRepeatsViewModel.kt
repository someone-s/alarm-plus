package com.eden.alarmplus.ui.main.repeats

import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmInfo
import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.group.feature.DayOfWeekFeature
import com.eden.alarmplus.data.alarms.structure.state.StateType
import com.eden.alarmplus.data.datetime.repository.TimeTickRepository
import com.eden.alarmplus.logic.access.StateHookProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.uuid.Uuid

val mockRepeatsViewModel = RepeatsViewModel(
    groupAlarmsRepository = object : GroupAlarmsRepository {
        override suspend fun notify(notification: GroupAlarmsRepository.ChangeNotification) {}

        override fun getAllGroupsStream():Flow<List<GroupData>> = flowOf(listOf(
            GroupData(
                info = GroupInfo(
                    name = "Morning alarms",
                    features = listOf(
                        DayOfWeekFeature(DayOfWeekFlags.merge(
                            DayOfWeekFlags.MONDAY,
                            DayOfWeekFlags.TUESDAY,
                            DayOfWeekFlags.WEDNESDAY
                        ))
                    ),
                    state = StateType.ENABLED_ACTIVE,
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
            GroupData(
                info = GroupInfo(
                    name = "Lunch alerts",
                    features = emptyList(),
                    state = StateType.DISABLED,
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