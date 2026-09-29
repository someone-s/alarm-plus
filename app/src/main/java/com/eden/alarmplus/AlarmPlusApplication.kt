package com.eden.alarmplus

import android.app.Application
import com.eden.alarmplus.data.AppContainer
import com.eden.alarmplus.data.AppDataContainer
import com.eden.alarmplus.logic.StateProcessor
import com.eden.alarmplus.logic.WakeScheduler
import com.eden.alarmplus.alert.notification.ChannelContainer
import com.eden.alarmplus.alert.notification.NotificationScope
import java.time.LocalDateTime
import kotlin.uuid.Uuid

class AlarmPlusApplication: Application() {
    lateinit var container: AppContainer

    val stateProcessor: StateProcessor by lazy {
        assert(this::container.isInitialized)
        StateProcessor(this, container.stateRepository, wakeScheduler)
    }

    private val wakeScheduler: WakeScheduler by lazy {
        assert(this::container.isInitialized)
        WakeScheduler(this, container.stateRepository)
    }

    val notificationScope: NotificationScope by lazy {
        NotificationScope(this, channelContainer)
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