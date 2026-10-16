package com.eden.alarmplus.logic.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eden.alarmplus.AlarmPlusApplication
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SystemReceiver: BroadcastReceiver() {

    class InvalidActionException: RuntimeException()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {}
            Intent.ACTION_TIMEZONE_CHANGED -> {}
            else -> throw InvalidActionException()
        }

        val application = context.applicationContext as AlarmPlusApplication
        val notificationRepository = application.container.notificationRepository
        val stateProcessor = application.logicScope.stateProcessor

        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                notificationRepository.reset()
                stateProcessor.initializeState(LocalDateTime.now())
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