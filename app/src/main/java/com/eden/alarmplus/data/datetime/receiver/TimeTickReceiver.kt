package com.eden.alarmplus.data.datetime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class TimeTickReceiver: BroadcastReceiver() {
    val timeTicked = MutableSharedFlow<Unit>()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                timeTicked.emit(Unit)
            }
            catch (e: Exception) {
                pendingResult.resultData = e.message
            }
            finally {
                pendingResult.finish()
            }
        }
    }
}