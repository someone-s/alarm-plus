package com.eden.alarmplus.ui.notification

import android.app.PendingIntent
import android.content.Intent
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
import com.eden.alarmplus.alert.AlertLogic
import com.eden.alarmplus.alert.AlertService
import com.eden.alarmplus.ui.theme.AlarmPlusTheme


class NotificationActivity: ComponentActivity() {
    companion object {
        /**
         * Extra of type [Int] notification id
         */
        const val EXTRA_NOTIFICATION_ID = "${BuildConfig.APPLICATION_ID}.EXTRA_NOTIFICATION_ID"
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
    }

    var notificationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1).also {
            if (it < 0) throw IllegalArgumentException("Notification id missing or less than 0 in intent.")
        }

        val snoozePendingIntent =  IntentCompat.getParcelableExtra(
            intent, EXTRA_SNOOZE_PENDING_INTENT,PendingIntent::class.java)

        val dismissPendingIntent = IntentCompat.getParcelableExtra(
            intent, EXTRA_DISMISS_PENDING_INTENT, PendingIntent::class.java)

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
                        onSnooze = snoozePendingIntent?.createCallback(),
                        onDismiss =  dismissPendingIntent?.createCallback(),
                    )
                }
            }
        }
    }

    override fun onPause() {
        if (notificationId < 0) {
            Log.e("$javaClass", "Notification id not set or less than zero.")
            return
        }

        ContextCompat.startForegroundService(this,
            Intent(this, AlertService::class.java).apply {
                action = AlertLogic.ACTION_FULL_SCREEN_PAUSE
                putExtra(AlertLogic.EXTRA_NOTIFICATION_ID, notificationId)
            }
        )

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        if (notificationId < 0) {
            Log.e("$javaClass", "Notification id not set or less than zero.")
            return
        }

        ContextCompat.startForegroundService(this,
            Intent(this, AlertService::class.java).apply {
                action = AlertLogic.ACTION_FULL_SCREEN_RESUME
                putExtra(AlertLogic.EXTRA_NOTIFICATION_ID, notificationId)
            }
        )

    }
}