package com.eden.alarmplus.data.container

import android.content.Context
import com.eden.alarmplus.data.database.AppDatabase
import com.eden.alarmplus.data.repository.GroupAlarmsRepository
import com.eden.alarmplus.data.repository.OfflineGroupAlarmsRepository
import com.eden.alarmplus.data.repository.OfflineStateRepository
import com.eden.alarmplus.data.repository.StateRepository
import com.eden.alarmplus.main.logic.StateProcessor
import com.eden.alarmplus.main.logic.WakeScheduler
import java.time.LocalDateTime

class AppDataContainer(private val context: Context) : AppContainer {


    override val groupAlarmsRepository: GroupAlarmsRepository by lazy {
        OfflineGroupAlarmsRepository(
            groupAlarmsDao = AppDatabase.getDatabase(context).groupAlarmsDao(),
            notificationListener = notificationListener,
        )
    }

    private val notificationListener: OfflineGroupAlarmsRepository.NotificationListener by lazy {
        object : OfflineGroupAlarmsRepository.NotificationListener() {

            override suspend fun onNotification(notification: GroupAlarmsRepository.ChangeNotification) =
                when (notification) {
                    is GroupAlarmsRepository.ChangeNotification.Update,
                    is GroupAlarmsRepository.ChangeNotification.Delete -> {
                        stateProcessor.initializeState(LocalDateTime.now())
                    }
                }

        }
    }

    override val stateProcessor: StateProcessor by lazy {
        StateProcessor(stateRepository, wakeScheduler)
    }

    private val wakeScheduler: WakeScheduler by lazy {
        WakeScheduler(context, stateRepository)
    }

    override val stateRepository: StateRepository by lazy {
        OfflineStateRepository(AppDatabase.getDatabase(context).stateDao())
    }
}