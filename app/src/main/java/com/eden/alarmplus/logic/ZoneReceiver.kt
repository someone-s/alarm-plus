package com.eden.alarmplus.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ZoneReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        assert(intent.action == Intent.ACTION_TIMEZONE_CHANGED)
//        val timeZone =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                val id = intent?.getStringExtra(Intent.EXTRA_TIMEZONE)
//                if (id != null)
//                    TimeZone.getTimeZone(id)
//                else
//                    TimeZone.getDefault()
//            }
//            else
//                TimeZone.getDefault()
    }
}