package com.eden.alarmplus.alert.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.IntentCompat
import com.eden.alarmplus.AlarmPlusApplication
import com.eden.alarmplus.BuildConfig
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.notifications.repository.PendingIntentRepository
import com.eden.alarmplus.ui.notification.NotificationActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class AlarmNotificationScope(
    private val context: Context,
    private val pendingIntentRepository: PendingIntentRepository,
    private val channelContainer: ChannelContainer,
) {
    companion object {
        private const val REPO_SNOOZE_TAG = 1
        private const val REPO_DISMISS_TAG = 2
        private const val REPO_FULLSCREEN_TAG = 3
    }

    /**
     * @param identifyingValue
     * The value to identify PendingIntent tied to
     * Intended there will always only be one notification per group
     * so tag can be the group's uuid
     * and id can be a fixed value
     */
    inner class Builder(
        private val group: GroupInfo,
        private val identifyingValue: String = group.uuid.toHexString(),
        private val onCancelledIntent: PendingIntent? = null,
    ) {

        var snoozeIntent: PendingIntent? = null
        var dismissIntent: PendingIntent? = null
        var includeFullScreen: Boolean = false
        var timeoutMs: Long? = null

        private suspend fun buildNotificationPendingIntent(repoTag: Int, notificationAction: String) = PendingIntent.getBroadcast(
            context,
            pendingIntentRepository.swapRequestCode(repoTag, identifyingValue),
            Intent(context, Receiver::class.java).apply {
                action = notificationAction
                putExtra(Receiver.EXTRA_IDENTIFYING_VALUE, identifyingValue)
                putExtra(Receiver.EXTRA_GROUP_UUID, group.uuid)
                putExtra(Receiver.EXTRA_ON_CANCELLED_INTENT, onCancelledIntent)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        suspend fun addSnooze() { snoozeIntent = buildNotificationPendingIntent(
            repoTag = REPO_SNOOZE_TAG,
            notificationAction = Receiver.ACTION_SNOOZE
        ) }

        suspend fun addDismiss() { dismissIntent = buildNotificationPendingIntent(
            repoTag = REPO_DISMISS_TAG,
            notificationAction = Receiver.ACTION_DISMISS
        ) }

        fun addFullScreen() { includeFullScreen = true }

        private suspend fun buildFullScreenIntent() = PendingIntent.getActivity(
            context,
            pendingIntentRepository.swapRequestCode(REPO_FULLSCREEN_TAG, identifyingValue),
            Intent(context, NotificationActivity::class.java).apply {
                putExtra(NotificationActivity.EXTRA_GROUP_UUID, group.uuid)

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

        suspend fun build(
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

                if (includeFullScreen)
                    @SuppressLint("FullScreenIntentPolicy")
                    setFullScreenIntent(
                        buildFullScreenIntent(),
                        true
                    )
            }.build()
        }
    }

    class Receiver: BroadcastReceiver() {
        companion object {
            /**
             * Extra of type [String] identifying value of notification
             */
            const val EXTRA_IDENTIFYING_VALUE = "${BuildConfig.APPLICATION_ID}.EXTRA_IDENTIFYING_VALUE"

            /**
             * Extra of type [Uuid] representing the group to show
             */
            const val EXTRA_GROUP_UUID = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_UUID"

            const val ACTION_SNOOZE = "${BuildConfig.APPLICATION_ID}.ACTION_SNOOZE"

            const val ACTION_DISMISS = "${BuildConfig.APPLICATION_ID}.ACTION_DISMISS"

            /**
             * Extra of type [android.os.Parcelable] that is a [android.app.PendingIntent];
             * Can be null/not set;
             * Can be canceled when used
             */
            const val EXTRA_ON_CANCELLED_INTENT = "${BuildConfig.APPLICATION_ID}.EXTRA_ON_CANCELLED_INTENT"
        }

        override fun onReceive(context: Context, intent: Intent) {

            val application = context.applicationContext as AlarmPlusApplication
            val pendingIntentRepository = application.container.pendingIntentRepository
            val stateProcessor = application.stateProcessor

            val pendingResult = goAsync()
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                try {
                    intent.getStringExtra(EXTRA_IDENTIFYING_VALUE)?.also {
                        pendingIntentRepository.releaseRequestCode(REPO_SNOOZE_TAG, it)
                        pendingIntentRepository.releaseRequestCode(REPO_DISMISS_TAG, it)
                        pendingIntentRepository.releaseRequestCode(REPO_FULLSCREEN_TAG, it)
                    }


                    val groupUuid = IntentCompat.getSerializableExtra(
                        intent,
                        EXTRA_GROUP_UUID,
                        Uuid::class.java
                    )
                    if (groupUuid == null) {
                        Log.e("$javaClass", "Missing EXTRA_GROUP_UUID.")
                        return@launch
                    }

                    when (intent.action) {
                        ACTION_SNOOZE -> {
                            // do nothing, alarm stays active
                        }

                        ACTION_DISMISS -> {
                            stateProcessor.dismissGroup(groupUuid)
                        }

                        else -> {
                            Log.e("$javaClass", "Unknown action.")
                        }
                    }

                    IntentCompat.getParcelableExtra(
                        intent,
                        EXTRA_ON_CANCELLED_INTENT,
                        PendingIntent::class.java
                    )?.also {
                        try {
                            it.send()
                        } catch (e: PendingIntent.CanceledException) {
                            Log.i("$javaClass", "Intent already cancelled, no effect.", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("$javaClass", "Exception while calling stateProcessor.", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}