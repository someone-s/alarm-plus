package com.eden.alarmplus.alert

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.eden.alarmplus.AlarmPlusApplication

class AlertService: LifecycleService() {

    private var alertLogic: AlertLogic? = null

    override fun onCreate() {
        super.onCreate()
        alertLogic = AlertLogic(
            this,
            (application as AlarmPlusApplication).alertScope.notificationScope,
            (application as AlarmPlusApplication).alertScope.hapticAudioPlayer,
            (application as AlarmPlusApplication).alertScope.vibrationScope,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Intent can be null from recrated service
        if (intent != null)
            try {
                alertLogic?.processRequest(startId, intent)
            } catch (e: Exception) {
                Log.e("$javaClass", e.message, e)
            }

        // Restart if killed after this return
        return START_STICKY
    }

    override fun onTimeout(startId: Int) {
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

}