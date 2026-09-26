package com.eden.alarmplus.data.container

import com.eden.alarmplus.data.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.repository.StateRepository
import com.eden.alarmplus.logic.StateProcessor

interface AppContainer {
    val groupAlarmsRepository: GroupAlarmsRepository
    val stateProcessor: StateProcessor
    val stateRepository: StateRepository
}