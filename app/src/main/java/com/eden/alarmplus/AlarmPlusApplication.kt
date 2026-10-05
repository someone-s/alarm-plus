package com.eden.alarmplus

import android.app.Application
import androidx.core.content.ContextCompat
import com.eden.alarmplus.alert.hapticaudio.HapticAudioScope
import com.eden.alarmplus.data.AppContainer
import com.eden.alarmplus.data.AppDataContainer
import com.eden.alarmplus.logic.StateProcessor
import com.eden.alarmplus.logic.WakeScheduler
import com.eden.alarmplus.alert.notification.ChannelContainer
import com.eden.alarmplus.alert.notification.NotificationScope
import com.eden.alarmplus.alert.vibration.VibrationScope
import com.eden.alarmplus.logic.LocalStateProcessor
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import kotlin.uuid.Uuid

class AlarmPlusApplication: Application() {

    companion object {
        const val ATTRIBUTION_TAG_PLAY_AUDIBLE_ALARM = "playAudibleAlarm"
        val DISMISS_WINDOW = 1.hours.toJavaDuration()
    }

    lateinit var container: AppContainer

    val stateProcessor: StateProcessor by lazy {
        assert(this::container.isInitialized)
        LocalStateProcessor(this, container.stateRepository, wakeScheduler, DISMISS_WINDOW)
    }

    private val wakeScheduler: WakeScheduler by lazy {
        assert(this::container.isInitialized)
        WakeScheduler(this, container.stateRepository, DISMISS_WINDOW)
    }

    val notificationScope: NotificationScope by lazy {
        NotificationScope(this, channelContainer)
    }

    val hapticAudioPlayer: HapticAudioScope by lazy {
        HapticAudioScope(
            ContextCompat.createAttributionContext(this, ATTRIBUTION_TAG_PLAY_AUDIBLE_ALARM)
        )
    }

    val vibrationScope: VibrationScope by lazy {
        VibrationScope(this)
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