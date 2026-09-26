package com.eden.alarmplus.ui.notification

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eden.alarmplus.BuildConfig
import com.eden.alarmplus.ui.theme.AlarmPlusTheme


class NotificationActivity: ComponentActivity() {
    companion object {
        /**
         * Extra of type [kotlin.uuid.Uuid] representing the group to show
         */
        const val EXTRA_GROUP_UUID = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_UUID"

        /**
         * Extra of type [Boolean] determining whether to show snooze option
         */
        const val EXTRA_IS_LAST_IN_GROUP = "${BuildConfig.APPLICATION_ID}.EXTRA_IS_LAST_IN_GROUP"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AlarmPlusTheme {
            }
        }
    }
}