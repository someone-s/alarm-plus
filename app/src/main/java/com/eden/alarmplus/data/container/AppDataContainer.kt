package com.eden.alarmplus.data.container

import android.content.Context
import com.eden.alarmplus.data.database.GroupAlarmsDatabase
import com.eden.alarmplus.data.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.repository.OfflineGroupAlarmsRepository

class AppDataContainer(private val context: Context): AppContainer {
    override val groupAlarmsRepository: GroupAlarmsRepository by lazy {
        OfflineGroupAlarmsRepository(GroupAlarmsDatabase.getDatabase(context).groupAlarmsDao())
    }
}