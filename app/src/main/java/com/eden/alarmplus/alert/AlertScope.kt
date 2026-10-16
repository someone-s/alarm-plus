package com.eden.alarmplus.alert

import android.content.Context
import androidx.core.content.ContextCompat
import com.eden.alarmplus.AlarmPlusApplication.Companion.ATTRIBUTION_TAG_PLAY_AUDIBLE_ALARM
import com.eden.alarmplus.alert.hapticaudio.HapticAudioScope
import com.eden.alarmplus.alert.notification.ChannelContainer
import com.eden.alarmplus.alert.notification.AlertNotificationScope
import com.eden.alarmplus.alert.notification.ForewarnNotificationScope
import com.eden.alarmplus.alert.vibration.VibrationScope

class AlertScope(
    context: Context
) {

    val alertNotificationScope: AlertNotificationScope by lazy {
        AlertNotificationScope(context, channelContainer)
    }

    val forewarnNotificationScope: ForewarnNotificationScope by lazy {
        ForewarnNotificationScope(context, channelContainer)
    }

    val hapticAudioScope: HapticAudioScope by lazy {
        HapticAudioScope(
            ContextCompat.createAttributionContext(context, ATTRIBUTION_TAG_PLAY_AUDIBLE_ALARM)
        )
    }

    val vibrationScope: VibrationScope by lazy {
        VibrationScope(context)
    }

    val channelContainer: ChannelContainer by lazy {
        ChannelContainer(context)
    }
}