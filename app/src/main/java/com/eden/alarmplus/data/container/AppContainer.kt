package com.eden.alarmplus.data.container

import com.eden.alarmplus.data.repository.GroupAlarmsRepository

interface AppContainer {
    val groupAlarmsRepository: GroupAlarmsRepository
}