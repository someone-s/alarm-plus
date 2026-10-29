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
import kotlin.uuid.Uuid

class DismissReceiver: BroadcastReceiver() {

    companion object {
        /**
         * [Serializable] of type [Uuid], required
         */
        const val EXTRA_GROUP_UUID = "${BuildConfig.APPLICATION_ID}.EXTRA_GROUP_UUID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val logicScope = (context.applicationContext as AlarmPlusApplication).logicScope
        val groupUuid = IntentCompat.getSerializableExtra(intent, EXTRA_GROUP_UUID, Uuid::class.java)
            ?: throw IllegalArgumentException("Group Uuid not provided.")

        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                logicScope.stateProcessor.dismissGroup(groupUuid)
                logicScope.listener?.onDismiss(context, groupUuid)
            }
            catch (e: Exception) {
                Log.e("$javaClass", e.message, e)
            }
            finally {
                pendingResult.finish()
            }
        }
    }
}