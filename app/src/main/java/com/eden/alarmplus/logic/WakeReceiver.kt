package com.eden.alarmplus.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.eden.alarmplus.AlarmPlusApplication
import com.eden.alarmplus.BuildConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class WakeReceiver: BroadcastReceiver() {
    companion object {
        /**
         * Name of a string extra
         * the value must be a ISO8601 formatted string in local date time
         */
        const val EXTRA_TO_DATE_TIME = "${BuildConfig.APPLICATION_ID}.EXTRA_TO_DATE_TIME"
    }
    override fun onReceive(context: Context, intent: Intent) {

        val toDateTime = intent.getStringExtra(EXTRA_TO_DATE_TIME).let {
            if (it != null) {
                Log.w("$javaClass", "Supplied extra is null.")
                return@let LocalDateTime.parse(it)
            }
            else
                return@let LocalDateTime.now()

        }

        val alarmPlusApplication = context.applicationContext as AlarmPlusApplication
        val stateProcessor = alarmPlusApplication.stateProcessor

        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                stateProcessor.iterateState(toDateTime)
                pendingResult.resultCode = 0
            }
            catch (e: Exception) {
                pendingResult.resultCode = -1
                pendingResult.resultData = e.message
            }
            finally {
                pendingResult.finish()
            }
        }
    }
}