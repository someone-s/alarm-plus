package com.eden.alarmplus.alert.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.ui.notification.NotificationActivity

class NotificationScope(
    private val context: Context,
    private val channelContainer: ChannelContainer,
) {

    private data class FullScreenInfo(val notificationId: Int,
        val fullScreenRequestCode: Int,
    )

    inner class Builder(
        private val group: GroupInfo,
    ) {
        private var snoozeIntent: PendingIntent? = null
        private var dismissIntent: PendingIntent? = null
        private var fullScreenInfo: FullScreenInfo? = null
        private var timeoutMs: Long? = null


        fun addSnoozeIntent(pendingIntent: PendingIntent) { snoozeIntent = pendingIntent }

        fun addDismissIntent(pendingIntent: PendingIntent) { dismissIntent = pendingIntent }

        fun addFullScreen(notificationId: Int, fullScreenRequestCode: Int = notificationId) { fullScreenInfo = FullScreenInfo(
            notificationId = notificationId,
            fullScreenRequestCode = fullScreenRequestCode,
        ) }

        private fun buildFullScreenIntent(info: FullScreenInfo) = PendingIntent.getActivity(
            context,
            info.fullScreenRequestCode,
            Intent(context, NotificationActivity::class.java).apply {
                putExtra(NotificationActivity.EXTRA_NOTIFICATTION_ID, info.notificationId)

                if (snoozeIntent != null)
                    putExtra(NotificationActivity.EXTRA_SNOOZE_PENDING_INTENT, snoozeIntent)
                if (dismissIntent != null)
                    putExtra(NotificationActivity.EXTRA_DISMISS_PENDING_INTENT, dismissIntent)

                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        fun addTimeout(timeoutMs: Long) { this.timeoutMs = timeoutMs }

        fun build(
            notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(context)
        ): Notification = with(notificationManagerCompat) {

            // Ensure channel exists
            createNotificationChannel(channelContainer.alarmChannel)

            return@with NotificationCompat.Builder(context, ChannelContainer.ALARM_CHANNEL_ID).apply {

                // These needed on older platform that does not support notification channel
                setPriority(NotificationCompat.PRIORITY_HIGH)
                setSound(null)
                setVibrate(null)

                setSmallIcon(R.drawable.icon_full)
                setContentTitle(group.name ?: context.getString(R.string.alert_notification_alarmnotificationbuilder_placeholder))

                val capturedTimeoutMs = timeoutMs
                if (capturedTimeoutMs != null)
                    setTimeoutAfter(capturedTimeoutMs)

                if (snoozeIntent != null)
                    addAction(
                        R.drawable.snooze,
                        context.getString(R.string.notifications_snoozeaction_title),
                        snoozeIntent
                    )

                if (dismissIntent != null)
                    addAction(
                        R.drawable.close,
                        context.getString(R.string.notifications_dismissaction_title),
                        dismissIntent
                    )

                val capturedFullScreenInfo = fullScreenInfo
                if (capturedFullScreenInfo != null)
                    @SuppressLint("FullScreenIntentPolicy")
                    setFullScreenIntent(
                        buildFullScreenIntent(capturedFullScreenInfo),
                        true
                    )
            }.build()
        }
    }
}