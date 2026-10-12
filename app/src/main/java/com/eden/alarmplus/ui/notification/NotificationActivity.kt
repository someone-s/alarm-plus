package com.eden.alarmplus.ui.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.eden.alarmplus.BuildConfig
import com.eden.alarmplus.ui.theme.AlarmPlusTheme
import java.time.LocalTime


class NotificationActivity: ComponentActivity() {
    companion object {
        /**
         * Use this action in an activity intent to start a running notification activity
         * Requires:
         * - [EXTRA_ID]
         */
        const val ACTION_DISPLAY = "${BuildConfig.APPLICATION_ID}.ACTION_DISPLAY"

        /**
         * Extra of type [android.os.Parcelable] that is a [android.app.PendingIntent];
         * Can be null/not set;
         * Can be canceled when used
         */
        const val EXTRA_SNOOZE_PENDING_INTENT = "${BuildConfig.APPLICATION_ID}.EXTRA_SNOOZE_PENDING_INTENT"

        /**
         * Extra of type [android.os.Parcelable] that is a [android.app.PendingIntent];
         * Can be null/not set;
         * Can be canceled when used
         */
        const val EXTRA_DISMISS_PENDING_INTENT = "${BuildConfig.APPLICATION_ID}.EXTRA_DISMISS_PENDING_INTENT"

        /**
         * Extra of type [String];
         * Can be null/not set;
         */
        const val EXTRA_GROUP_NAME = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_NAME"

        /**
         * Extra of type [Serializable] that is a [java.time.LocalTime];
         * Can be null/not set;
         */
        const val EXTRA_ALARM_TIME = "${BuildConfig.APPLICATION_ID}.EXTRA_ALARM_TIME"

        /**
         * Extra of type [Integer];
         * Required
         */
        const val EXTRA_ID = "${BuildConfig.APPLICATION_ID}.EXTRA_ID"


        /**
         * Use this action in a broadcast intent to cancel any running notification activity;
         * Requires:
         * - [EXTRA_ID]
         */
        const val ACTION_CANCEL = "${BuildConfig.APPLICATION_ID}.ACTION_CANCEL"
    }

    inner class CancelReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val selfId = this@NotificationActivity.intent.getIntExtra(EXTRA_ID, -1)
            val targetId = intent.getIntExtra(EXTRA_ID, -1)
            if (selfId != -1 && targetId != -1 && selfId == targetId)
                this@NotificationActivity.finish()
        }
    }

    private val cancelReceiver = CancelReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContextCompat.registerReceiver(
            this,
            cancelReceiver,
            IntentFilter(ACTION_CANCEL),
            ContextCompat.RECEIVER_EXPORTED)

        if (intent.action == ACTION_DISPLAY)
            display()
        else
            finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        this.unregisterReceiver(cancelReceiver)
    }

    private fun display() {
        val snoozePendingIntent = IntentCompat.getParcelableExtra(
            intent, EXTRA_SNOOZE_PENDING_INTENT, PendingIntent::class.java
        )

        val dismissPendingIntent = IntentCompat.getParcelableExtra(
            intent, EXTRA_DISMISS_PENDING_INTENT, PendingIntent::class.java
        )

        val groupName = intent.getStringExtra(EXTRA_GROUP_NAME)

        val alarmTime = IntentCompat.getSerializableExtra(
            intent, EXTRA_ALARM_TIME, LocalTime::class.java
        )

        fun PendingIntent.createCallback(): () -> Unit = {
            try {
                this.send()
            } catch (e: PendingIntent.CanceledException) {
                Log.i("$javaClass", "Intent already cancelled, no effect.", e)
            }

            finishAfterTransition()
        }

        enableEdgeToEdge()
        setContent {
            AlarmPlusTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotificationContent(
                        title = groupName,
                        time = alarmTime,
                        onSnooze = snoozePendingIntent?.createCallback(),
                        onDismiss = dismissPendingIntent?.createCallback(),
                    )
                }
            }
        }
    }
}