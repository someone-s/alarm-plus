package com.eden.alarmplus.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.eden.alarmplus.data.alarms.database.AlarmsDatabase
import com.eden.alarmplus.data.alarms.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.alarms.repository.OfflineGroupAlarmsRepository
import com.eden.alarmplus.data.alarms.repository.OfflineStateRepository
import com.eden.alarmplus.data.alarms.repository.StateRepository
import com.eden.alarmplus.data.datetime.receiver.TimeChangeReceiver
import com.eden.alarmplus.data.datetime.receiver.TimeTickReceiver
import com.eden.alarmplus.data.datetime.repository.DateFormatRepository
import com.eden.alarmplus.data.datetime.repository.ReceiverDateFormatRepository
import com.eden.alarmplus.data.datetime.repository.ReceiverTimeFormatRepository
import com.eden.alarmplus.data.datetime.repository.ReceiverTimeTickRepository
import com.eden.alarmplus.data.datetime.repository.TimeFormatRepository
import com.eden.alarmplus.data.datetime.repository.TimeTickRepository
import com.eden.alarmplus.data.notifications.database.NotificationDatabase
import com.eden.alarmplus.data.notifications.repository.NotificationRepository
import com.eden.alarmplus.data.notifications.repository.OfflineNotificationRepository
import kotlin.uuid.Uuid

class AppDataContainer(
    private val context: Context,
    private val groupAlarmListener: GroupAlarmListener? = null,
) : AppContainer {

    override val groupAlarmsRepository: GroupAlarmsRepository by lazy {
        OfflineGroupAlarmsRepository(
            groupsDao = AlarmsDatabase.getDatabase(context).groupsDao(),
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
        OfflineStateRepository(AlarmsDatabase.getDatabase(context).stateDao())
    }

    override val notificationRepository: NotificationRepository by lazy {
        OfflineNotificationRepository(NotificationDatabase.getDatabase(context).notificationDao())
    }

    private val timeChangeReceiver: TimeChangeReceiver by lazy {
        TimeChangeReceiver().also {
            context.registerReceiver(it, IntentFilter(Intent.ACTION_TIME_CHANGED))
        }
    }

    override val dateFormatRepository: DateFormatRepository by lazy {
        ReceiverDateFormatRepository(context, timeChangeReceiver)
    }

    override val timeFormatRepository: TimeFormatRepository by lazy {
        ReceiverTimeFormatRepository(context, timeChangeReceiver)
    }

    override val timeTickRepository: TimeTickRepository by lazy {
        val timeTickReceiver = TimeTickReceiver()
        context.registerReceiver(timeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        ReceiverTimeTickRepository(timeTickReceiver)
    }
}