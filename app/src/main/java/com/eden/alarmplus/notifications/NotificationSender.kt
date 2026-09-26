package com.eden.alarmplus.notifications

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
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.feature.AudioFeature
import com.eden.alarmplus.data.notifications.repository.PendingIntentRepository
import com.eden.alarmplus.ui.notification.NotificationActivity
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

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
        groupUuid: Uuid,
        alarm: AlarmData,
        isLastInGroup: Boolean,
    ) {

        // There will always only be one notification per group
        // so tag is the group's uuid
        // and id can be a fixed value
        val groupUuidString = groupUuid.toHexString()

        suspend fun buildNotificationAction(
            repoTag: Int,
            notificationTag: String,
            icon: Int,
            title: Int,
            response: Int,
        ) = NotificationCompat.Action.Builder(
            icon,
            context.getString(title),
            PendingIntent.getBroadcast(
                context,
                pendingIntentRepository.swapRequestCode(repoTag, groupUuidString),
                Intent(context, NotificationReceiver::class.java).apply {
                    putExtra(NotificationReceiver.EXTRA_NOTIFICATION_TAG, notificationTag)
                    putExtra(NotificationReceiver.EXTRA_GROUP_UUID, groupUuid)
                    putExtra(NotificationReceiver.EXTRA_RESPONSE, response)
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
        ).build()

        suspend fun buildSnoozeAction(notificationTag: String) = buildNotificationAction(
            REPO_SNOOZE_TAG,
            notificationTag,
            R.drawable.snooze,
            R.string.notifications_snoozeaction_title,
            NotificationReceiver.RESPONSE_SNOOZE,
        )

        suspend fun buildDismissAction(notificationTag: String) = buildNotificationAction(
            REPO_DISMISS_TAG,
            notificationTag,
            R.drawable.close,
            R.string.notifications_dismissaction_title,
            NotificationReceiver.RESPONSE_DISMISS,
        )


        suspend fun buildFullScreenPendingIntent() = PendingIntent.getActivity(
            context,
            pendingIntentRepository.swapRequestCode(REPO_FULLSCREEN_TAG, groupUuidString),
            Intent(context, NotificationActivity::class.java).apply {
                putExtra(NotificationActivity.EXTRA_GROUP_UUID, groupUuid)
                alarm.getFeature<AudioFeature>()
                putExtra(NotificationActivity.EXTRA_IS_LAST_IN_GROUP, isLastInGroup)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        with (NotificationManagerCompat.from(context)) {

            // There will always only be one notification per group
            // so tag is the group's uuid
            // and id can be a fixed value
            val tag = groupUuidString

            // Cancel previous notification of this group, if exist
            cancel(tag, 0)

            // Ensure channel exists
            createNotificationChannel(channelContainer.alarmChannel)

            // Construct notification
            val builder = NotificationCompat.Builder(context, ChannelContainer.ALARM_CHANNEL_ID).apply {
                // needed on older platform that does not support notification channel
                setPriority(NotificationCompat.PRIORITY_HIGH)

                setSmallIcon(R.drawable.icon_full)
                setContentTitle("Alarm!!")

                setTimeoutAfter(10.minutes.inWholeMilliseconds)

                if (!isLastInGroup)
                    addAction(buildSnoozeAction(tag))

                addAction(buildDismissAction(tag))

                if (canUseFullScreenIntent())
                    @SuppressLint("FullScreenIntentPolicy")
                    setFullScreenIntent(buildFullScreenPendingIntent(), true)
            }

            // Exit if no permission to send notifications
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                Log.w("$javaClass", "Missing POST_NOTIFICATIONS permission")
                return@with
            }

            notify(tag, 0, builder.build())
        }
    }
}