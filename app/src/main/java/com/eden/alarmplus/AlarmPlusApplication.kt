package com.eden.alarmplus

import android.app.Application
import android.content.Context
import android.content.Intent
import com.eden.alarmplus.alert.AlertLogic.Companion.ACTION_DISMISS
import com.eden.alarmplus.alert.AlertLogic.Companion.EXTRA_GROUP_UUID
import com.eden.alarmplus.alert.AlertScope
import com.eden.alarmplus.alert.AlertService
import com.eden.alarmplus.data.AppContainer
import com.eden.alarmplus.data.AppDataContainer
import com.eden.alarmplus.logic.LogicScope
import com.eden.alarmplus.logic.access.LogicListener
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import kotlin.uuid.Uuid

class AlarmPlusApplication: Application() {

    companion object {
        const val ATTRIBUTION_TAG_PLAY_AUDIBLE_ALARM = "playAudibleAlarm"
        val DISMISS_WINDOW = 1.hours.toJavaDuration()
    }

    lateinit var container: AppContainer

    val logicScope: LogicScope by lazy {
        assert(this::container.isInitialized)
        LogicScope(
            context = this,
            container = container,
            listener = object : LogicListener {
                override fun onInitialize(context: Context) {}

                override fun onDismiss(context: Context, groupUuid: Uuid) {
                    context.startService(
                        Intent(context, AlertService::class.java).apply {
                            action = ACTION_DISMISS
                            putExtra(EXTRA_GROUP_UUID, groupUuid)
                        }
                    )
                }

            }
        )
    }
    
    val alertScope: AlertScope by lazy {
        AlertScope(this)
    }

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(
            context = this,
            groupAlarmListener = object : AppDataContainer.GroupAlarmListener {
                override suspend fun onUpdate(groupUuid: Uuid) {
                    logicScope.stateHookProvider.initializeState(LocalDateTime.now())
                }

                override suspend fun onDelete(groupUuid: Uuid) {
                    logicScope.stateHookProvider.initializeState(LocalDateTime.now())
                }
            })
    }
}