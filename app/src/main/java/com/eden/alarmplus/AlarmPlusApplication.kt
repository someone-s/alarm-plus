package com.eden.alarmplus

import android.app.Application
import com.eden.alarmplus.data.AppContainer
import com.eden.alarmplus.data.AppDataContainer
import com.eden.alarmplus.logic.StateProcessor
import com.eden.alarmplus.logic.WakeScheduler
import com.eden.alarmplus.alert.notification.ChannelContainer
import com.eden.alarmplus.alert.notification.NotificationSender
import java.time.LocalDateTime
import kotlin.uuid.Uuid

class AlarmPlusApplication: Application() {
    lateinit var container: AppContainer

    val stateProcessor: StateProcessor by lazy {
        assert(this::container.isInitialized)
        StateProcessor(container.stateRepository, wakeScheduler, notificationSender)
    }

    private val wakeScheduler: WakeScheduler by lazy {
        assert(this::container.isInitialized)
        WakeScheduler(this, container.stateRepository)
    }

    private val notificationSender: NotificationSender by lazy {
        NotificationSender(this, container.pendingIntentRepository, channelContainer)
    }

    val channelContainer: ChannelContainer by lazy {
        ChannelContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(
            context = this,
            groupAlarmListener = object : AppDataContainer.GroupAlarmListener {
                override suspend fun onUpdate(groupUuid: Uuid) {
                    stateProcessor.initializeState(LocalDateTime.now())
                }

                override suspend fun onDelete(groupUuid: Uuid) {
                    stateProcessor.initializeState(LocalDateTime.now())
                }
            })
    }
}