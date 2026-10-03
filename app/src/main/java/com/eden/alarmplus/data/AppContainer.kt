package com.eden.alarmplus.data

import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.repository.StateRepository
import com.eden.alarmplus.data.datetime.repository.TimeFormatRepository
import com.eden.alarmplus.data.datetime.repository.TimeTickRepository

interface AppContainer {
    val groupAlarmsRepository: GroupAlarmsRepository
    val stateRepository: StateRepository
    val timeFormatRepository: TimeFormatRepository
    val timeTickRepository: TimeTickRepository
}