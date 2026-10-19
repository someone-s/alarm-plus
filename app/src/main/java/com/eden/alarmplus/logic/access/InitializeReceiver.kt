package com.eden.alarmplus.logic.access

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.IntentCompat
import com.eden.alarmplus.AlarmPlusApplication
import com.eden.alarmplus.BuildConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class InitializeReceiver: BroadcastReceiver() {
    companion object {
        /**
         * [Serializable] of type [LocalDateTime], required
         */
        const val EXTRA_DATE_TIME = "${BuildConfig.APPLICATION_ID}.EXTRA_DATE_TIME"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val logicScope = (context.applicationContext as AlarmPlusApplication).logicScope
        val dateTime =
            IntentCompat.getSerializableExtra(intent, EXTRA_DATE_TIME, LocalDateTime::class.java)
                ?: throw IllegalArgumentException("Group Uuid not provided.")

        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                logicScope.stateProcessor.initializeState(dateTime)
                logicScope.listener?.onInitialize(context)
            } catch (e: Exception) {
                Log.e("$javaClass", e.message, e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}