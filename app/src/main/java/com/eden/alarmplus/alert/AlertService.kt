package com.eden.alarmplus.alert

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Parcelable
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import com.eden.alarmplus.AlarmPlusApplication
import com.eden.alarmplus.BuildConfig
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.minutes

class AlertService: Service() {

    companion object {
        private const val FOREGROUND_SERVICE_ID = 100
        private const val START_COMMAND_INTENT = "${BuildConfig.APPLICATION_ID}.REQUEST_INTENT"

        /**
         * Extra of type [Parcelable] that is [com.eden.alarmplus.data.alarms.structure.group.GroupInfo]
         */
        const val EXTRA_GROUP_INFO = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_INFO"

        /**
         * Extra of type [Parcelable] that is [com.eden.alarmplus.data.alarms.structure.alarm.AlarmData]
         */
        const val EXTRA_ALARM_DATA = "${BuildConfig.APPLICATION_ID}.EXTRA_ALARM_DATA"

        /**
         * Extra of type [Boolean]
         */
        const val EXTRA_IS_LAST_IN_GROUP = "${BuildConfig.APPLICATION_ID}.EXTRA_IS_LAST_IN_GROUP"
    }

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    @OptIn(ExperimentalAtomicApi::class)
    private inner class ServiceHandler(looper: Looper): Handler(looper) {

        private var foregroundCount = 0

        /**
         * @param msg
         * [Message.arg1] is startId which will be used to call [stopSelf] when request finished
         * [Message.getData] is Bundle with the following keys:
         * - [START_COMMAND_INTENT]: a [android.os.Parcelable] of type [Intent], which contains:
         *   - [EXTRA_GROUP_INFO]
         *   - [EXTRA_ALARM_DATA]
         *   - [EXTRA_IS_LAST_IN_GROUP]
         */
        override fun handleMessage(msg: Message) {

            // Exit if no permission to send notifications
            if (ContextCompat.checkSelfPermission(
                    this@AlertService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                Log.w("$javaClass", "Missing POST_NOTIFICATIONS permission")
                return
            }

            val startId = msg.arg1
            val intent = BundleCompat.getParcelable(msg.data, START_COMMAND_INTENT, Intent::class.java)

            val alarmNotificationScope = (application as AlarmPlusApplication).alarmNotificationScope

            try {
                checkNotNull(intent)

                val groupInfo = IntentCompat.getParcelableExtra(intent, EXTRA_GROUP_INFO, GroupInfo::class.java)
                checkNotNull(groupInfo)

                val alarmData = IntentCompat.getParcelableExtra(intent, EXTRA_ALARM_DATA, AlarmData::class.java)
                checkNotNull(alarmData)

                assert(intent.hasExtra(EXTRA_IS_LAST_IN_GROUP))
                val isLastInGroup = intent.getBooleanExtra(EXTRA_IS_LAST_IN_GROUP, false)


                with(NotificationManagerCompat.from(this@AlertService)) {

                    val notification = runBlocking {
                        alarmNotificationScope.Builder(
                            group = groupInfo,
                        ).apply {
                            addTimeout(10.minutes.inWholeMilliseconds)

                            if (!isLastInGroup)
                                addSnooze()

                            addDismiss()

                            if (canUseFullScreenIntent())
                                addFullScreen()

                        }.build(this@with)
                    }

                    synchronized(this) {
                        val previousCount = foregroundCount++

                        if (previousCount == 0)
                            ServiceCompat.startForeground(
                                this@AlertService,
                                FOREGROUND_SERVICE_ID,
                                notification,
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
                                else 0,
                            )
                        else
                            notify(startId, notification)
                    }
                }

            } catch (e: Exception) {
                Log.e("$javaClass", e.message, e)
            }

        }
    }

    override fun onCreate() {
        HandlerThread("", Process.THREAD_PRIORITY_DISPLAY).apply {
            start()

            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // Start a job for each start request
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            msg.data = Bundle().apply {
                putParcelable(START_COMMAND_INTENT, intent)
            }
            serviceHandler?.sendMessage(msg)
        }

        // Restart if killed after this return
        return START_STICKY
    }

    override fun onTimeout(startId: Int) {
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? = null

}