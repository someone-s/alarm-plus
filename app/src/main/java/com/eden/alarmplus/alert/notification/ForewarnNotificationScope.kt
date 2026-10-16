package com.eden.alarmplus.alert.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo

class ForewarnNotificationScope(
    private val context: Context,
    private val channelContainer: ChannelContainer,
) {

    fun cancelNotification(
        notificationId: Int,
        notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
    ): Unit = with(notificationManager) {
        cancel(notificationId)
    }

    inner class Builder(
        private val group: GroupInfo,
    ) {
        private var dismissIntent: PendingIntent? = null

        fun addDismissIntent(pendingIntent: PendingIntent) { dismissIntent = pendingIntent }

        fun build(
            notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(context)
        ): Notification = with(notificationManagerCompat) {

            // Ensure channel exists
            createNotificationChannel(channelContainer.forewarnChannel)

            return@with NotificationCompat.Builder(context, ChannelContainer.FOREWARN_CHANNEL_ID).apply {

                // These needed on older platform that does not support notification channel
                setPriority(NotificationCompat.PRIORITY_LOW)
                setSound(null)
                setVibrate(null)

                setSmallIcon(R.drawable.icon_full)
                setContentTitle(context.getString(R.string.notifications_forewarnnotification_title))
                setContentText(group.name ?: context.getString(R.string.notifications_emptygroupname))

                if (dismissIntent != null) {
                    addAction(
                        R.drawable.close,
                        context.getString(R.string.notifications_dismissaction_title),
                        dismissIntent
                    )
                }
            }.build()
        }
    }
}