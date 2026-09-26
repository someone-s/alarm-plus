package com.eden.alarmplus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.eden.alarmplus.BuildConfig

class NotificationReceiver: BroadcastReceiver() {
    companion object {
        /**
         * Extra of type [String] tag of notification, id assumed to be 0, to cancel
         */
        const val EXTRA_NOTIFICATION_TAG = "${BuildConfig.APPLICATION_ID}.EXTRA_NOTIFICATION_TAG"

        /**
         * Extra of type [kotlin.uuid.Uuid] representing the group to show
         */
        const val EXTRA_GROUP_UUID = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_UUID"

        /**
         * Extra either [RESPONSE_SNOOZE] or [RESPONSE_DISMISS]
         */
        const val EXTRA_RESPONSE = "${BuildConfig.APPLICATION_ID}.EXTRA_RESPONSE"

        const val RESPONSE_SNOOZE = 1

        const val RESPONSE_DISMISS = 2
    }

    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra(EXTRA_NOTIFICATION_TAG).also {
            if (it == null) return@also
            with(NotificationManagerCompat.from(context)) {
                cancel(it, 0)
            }
        }

    }
}