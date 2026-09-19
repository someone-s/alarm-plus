package com.eden.alarmplus.data.container

import android.content.Context
import com.eden.alarmplus.data.database.GroupAlarmDatabase
import com.eden.alarmplus.data.repository.GroupAlarmRepository
import com.eden.alarmplus.data.repository.OfflineGroupAlarmRepository

class AppDataContainer(private val context: Context): AppContainer {
    override val groupAlarmRepository: GroupAlarmRepository by lazy {
        OfflineGroupAlarmRepository(GroupAlarmDatabase.getDatabase(context).groupAlarmDao())
    }
}