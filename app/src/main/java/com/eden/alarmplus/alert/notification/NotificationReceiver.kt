package com.eden.alarmplus.alert.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.IntentCompat
import com.eden.alarmplus.AlarmPlusApplication
import com.eden.alarmplus.BuildConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class NotificationReceiver: BroadcastReceiver() {
    companion object {
        /**
         * Extra of type [String] tag of notification, id assumed to be 0, to cancel
         */
        const val EXTRA_NOTIFICATION_TAG = "${BuildConfig.APPLICATION_ID}.EXTRA_NOTIFICATION_TAG"

        /**
         * Extra of type [Uuid] representing the group to show
         */
        const val EXTRA_GROUP_UUID = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_UUID"

        const val ACTION_SNOOZE = "${BuildConfig.APPLICATION_ID}.ACTION_SNOOZE"

        const val ACTION_DISMISS = "${BuildConfig.APPLICATION_ID}.ACTION_DISMISS"
    }

    override fun onReceive(context: Context, intent: Intent) {

        intent.getStringExtra(EXTRA_NOTIFICATION_TAG).also {
            if (it == null) return@also
            with(NotificationManagerCompat.from(context)) {
                cancel(it, 0)
            }
        }

        val groupUuid = IntentCompat.getSerializableExtra(intent, EXTRA_GROUP_UUID, Uuid::class.java)
        if (groupUuid == null) {
            Log.e("$javaClass", "Missing EXTRA_GROUP_UUID.")
            return
        }

        when (intent.action) {
            ACTION_SNOOZE -> {
                // do nothing, alarm stays active
            }
            ACTION_DISMISS -> {
                val application = context.applicationContext as AlarmPlusApplication
                val stateProcessor = application.stateProcessor

                val pendingResult = goAsync()
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    try {
                        stateProcessor.dismissGroup(groupUuid)
                    }
                    catch (e: Exception) {
                        Log.e("$javaClass", "Exception while calling stateProcessor.", e)
                    }
                    finally {
                        pendingResult.finish()
                    }
                }
            }
            else -> {
                Log.e("$javaClass", "Unknown action.")
            }
        }
    }
}