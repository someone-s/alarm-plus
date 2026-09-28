package com.eden.alarmplus.alert.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import com.eden.alarmplus.BuildConfig
import com.eden.alarmplus.R

class ChannelContainer(
    private val context: Context,
) {
    companion object {
        const val ALARM_CHANNEL_ID = "${BuildConfig.APPLICATION_ID}.ALARM_CHANNEL"
        const val SERVICE_CHANNEL_ID = "${BuildConfig.APPLICATION_ID}.SERVICE_CHANNEL"
    }

    val alarmChannel by lazy {
        NotificationChannelCompat.Builder(ALARM_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH).apply {
            setName(context.getString(R.string.notifications_alarmchannel_name))
            setDescription(context.getString(R.string.notifications_alarmchannel_description))
            setSound(null, null)
            setVibrationEnabled(false)
        }.build()
    }

    val serviceChannel by lazy {
        NotificationChannelCompat.Builder(SERVICE_CHANNEL_ID, NotificationManager.IMPORTANCE_LOW).apply {
            setName(context.getString(R.string.notifications_servicechannel_name))
            setDescription(context.getString(R.string.notifications_servicechannel_description))
            setSound(null, null)
            setVibrationEnabled(false)
        }.build()
    }
}