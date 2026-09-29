package com.eden.alarmplus.alert

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import android.util.Log
import androidx.core.os.BundleCompat
import com.eden.alarmplus.AlarmPlusApplication
import com.eden.alarmplus.BuildConfig
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class AlertService: Service() {

    companion object {
        private const val THREAD_NAME = "${BuildConfig.APPLICATION_ID}.AlertServiceThread"
        private const val START_COMMAND_INTENT = "${BuildConfig.APPLICATION_ID}.REQUEST_INTENT"

    }

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    private var alertLogic: AlertLogic? = null

    @OptIn(ExperimentalAtomicApi::class)
    private inner class ServiceHandler(looper: Looper): Handler(looper) {

        /**
         * @param msg
         * [Message.arg1] is startId, identify the specific request
         * [Message.getData] is Bundle with the following keys:
         * - [START_COMMAND_INTENT]: a [android.os.Parcelable] of type [Intent],
         *   see [AlertLogic] for the expected intent formats
         */
        override fun handleMessage(msg: Message) {

            val startId = msg.arg1
            val intent = BundleCompat.getParcelable(msg.data, START_COMMAND_INTENT, Intent::class.java)

            try {
                checkNotNull(intent)
                alertLogic?.processRequest(startId, intent)
            } catch (e: Exception) {
                Log.e("$javaClass", e.message, e)
            }
        }
    }

    override fun onCreate() {
        alertLogic = AlertLogic(
            this,
            (application as AlarmPlusApplication).stateProcessor,
            (application as AlarmPlusApplication).notificationScope,
        )

        HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_DISPLAY).apply {
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