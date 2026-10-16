package com.eden.alarmplus.data

import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.repository.StateRepository
import com.eden.alarmplus.data.datetime.repository.TimeFormatRepository
import com.eden.alarmplus.data.datetime.repository.TimeTickRepository
import com.eden.alarmplus.data.notifications.repository.NotificationRepository

interface AppContainer {
    val groupAlarmsRepository: GroupAlarmsRepository
    val stateRepository: StateRepository
    val notificationRepository: NotificationRepository
    val timeFormatRepository: TimeFormatRepository
    val timeTickRepository: TimeTickRepository
}