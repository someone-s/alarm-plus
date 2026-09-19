package com.eden.alarmplus.data.container

import com.eden.alarmplus.data.repository.GroupAlarmRepository

interface AppContainer {
    val groupAlarmRepository: GroupAlarmRepository
}