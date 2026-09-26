package com.eden.alarmplus.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eden.alarmplus.MainApplication
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
        const val EXTRA_TO_DATE_TIME = "extra-to-date-time"
    }
    override fun onReceive(context: Context, intent: Intent) {
        val mainApplication = context.applicationContext as MainApplication
        val stateProcessor = mainApplication.container.stateProcessor

        val toDateTime = LocalDateTime.parse(intent.getStringExtra(EXTRA_TO_DATE_TIME))

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