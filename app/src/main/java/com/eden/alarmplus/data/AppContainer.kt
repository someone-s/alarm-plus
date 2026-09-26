package com.eden.alarmplus.data

import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.repository.StateRepository

interface AppContainer {
    val groupAlarmsRepository: GroupAlarmsRepository
    val stateRepository: StateRepository
}