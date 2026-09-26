package com.eden.alarmplus.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StartupReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        assert(intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED)
    }
}