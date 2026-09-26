package com.eden.alarmplus.data

import android.content.Context
import com.eden.alarmplus.data.alarms.database.AppDatabase
import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.repository.OfflineGroupAlarmsRepository
import com.eden.alarmplus.data.alarms.repository.OfflineStateRepository
import com.eden.alarmplus.data.alarms.repository.StateRepository
import kotlin.uuid.Uuid

class AppDataContainer(
    private val context: Context,
    private val groupAlarmListener: GroupAlarmListener? = null,
) : AppContainer {

    override val groupAlarmsRepository: GroupAlarmsRepository by lazy {
        OfflineGroupAlarmsRepository(
            groupsWithAlarmsDao = AppDatabase.getDatabase(context).groupsWithAlarmsDao(),
            notificationListener = notificationListener,
        )
    }

    interface GroupAlarmListener {
        suspend fun onUpdate(groupUuid: Uuid)
        suspend fun onDelete(groupUuid: Uuid)
    }

    private val notificationListener: OfflineGroupAlarmsRepository.NotificationListener by lazy {
        object : OfflineGroupAlarmsRepository.NotificationListener() {
            override suspend fun onNotification(notification: GroupAlarmsRepository.ChangeNotification) {
                when (notification) {
                    is GroupAlarmsRepository.ChangeNotification.Update -> groupAlarmListener?.onUpdate(notification.groupUuid)
                    is GroupAlarmsRepository.ChangeNotification.Delete -> groupAlarmListener?.onDelete(notification.groupUuid)
                }
            }

        }
    }

    override val stateRepository: StateRepository by lazy {
        OfflineStateRepository(AppDatabase.getDatabase(context).stateDao())
    }
}