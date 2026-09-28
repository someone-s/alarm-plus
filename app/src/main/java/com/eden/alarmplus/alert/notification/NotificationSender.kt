package com.eden.alarmplus.alert.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.notifications.repository.PendingIntentRepository
import com.eden.alarmplus.ui.notification.NotificationActivity
import kotlin.time.Duration.Companion.minutes

class NotificationSender(
    private val context: Context,
    private val pendingIntentRepository: PendingIntentRepository,
    private val channelContainer: ChannelContainer,
) {
    companion object {
        private const val REPO_SNOOZE_TAG = 1
        private const val REPO_DISMISS_TAG = 2
        private const val REPO_FULLSCREEN_TAG = 3
    }

    suspend fun sendAlarmNotification(
        group: GroupInfo,
        isLastInGroup: Boolean,
    ) {

        // There will always only be one notification per group
        // so tag is the group's uuid
        // and id can be a fixed value
        val notificationTag = group.uuid.toHexString()

        suspend fun buildNotificationPendingIntent(repoTag: Int, notificationAction: String) = PendingIntent.getBroadcast(
            context,
            pendingIntentRepository.swapRequestCode(repoTag, notificationTag),
            Intent(context, NotificationReceiver::class.java).apply {
                action = notificationAction
                putExtra(NotificationReceiver.EXTRA_NOTIFICATION_TAG, notificationTag)
                putExtra(NotificationReceiver.EXTRA_GROUP_UUID, group.uuid)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        suspend fun buildFullScreenPendingIntent(
            snoozePendingIntent: PendingIntent?,
            dismissPendingIntent: PendingIntent?,
        ) = PendingIntent.getActivity(
            context,
            pendingIntentRepository.swapRequestCode(REPO_FULLSCREEN_TAG, notificationTag),
            Intent(context, NotificationActivity::class.java).apply {
                putExtra(NotificationActivity.EXTRA_GROUP_UUID, group.uuid)
                putExtra(NotificationActivity.EXTRA_IS_LAST_IN_GROUP, isLastInGroup)
                if (snoozePendingIntent != null)
                    putExtra(NotificationActivity.EXTRA_SNOOZE_PENDING_INTENT, snoozePendingIntent)
                if (dismissPendingIntent != null)
                    putExtra(NotificationActivity.EXTRA_DISMISS_PENDING_INTENT, dismissPendingIntent)

                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        suspend fun buildNotification(
            includeFullScreenIntent: Boolean,
        ) = NotificationCompat.Builder(context, ChannelContainer.ALARM_CHANNEL_ID).apply {

            // These needed on older platform that does not support notification channel
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setSound(null)
            setVibrate(null)

            setSmallIcon(R.drawable.icon_full)
            setContentTitle(group.name ?: "Untitled Alarm")

            setTimeoutAfter(10.minutes.inWholeMilliseconds)

            val snoozePendingIntent = if (!isLastInGroup)
                buildNotificationPendingIntent(REPO_SNOOZE_TAG, NotificationReceiver.ACTION_SNOOZE)
            else null

            val dismissPendingIntent =
                buildNotificationPendingIntent(REPO_DISMISS_TAG, NotificationReceiver.ACTION_DISMISS)

            if (snoozePendingIntent != null)
                addAction(
                    R.drawable.snooze,
                    context.getString(R.string.notifications_snoozeaction_title),
                    snoozePendingIntent
                )

            addAction(
                R.drawable.close,
                context.getString(R.string.notifications_dismissaction_title),
                dismissPendingIntent
            )

            if (includeFullScreenIntent)
                @SuppressLint("FullScreenIntentPolicy")
                setFullScreenIntent(
                    buildFullScreenPendingIntent(
                        snoozePendingIntent = snoozePendingIntent,
                        dismissPendingIntent = dismissPendingIntent,
                    ),
                    true
                )
        }.build()

        with (NotificationManagerCompat.from(context)) {


            // Cancel previous notification of this group, if exist
            cancel(notificationTag, 0)

            // Ensure channel exists
            createNotificationChannel(channelContainer.alarmChannel)

            // Exit if no permission to send notifications
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                Log.w("$javaClass", "Missing POST_NOTIFICATIONS permission")
                return@with
            }

            val notification = buildNotification(
                includeFullScreenIntent = canUseFullScreenIntent(),
            )

            notify(notificationTag, 0, notification)
        }
    }
}