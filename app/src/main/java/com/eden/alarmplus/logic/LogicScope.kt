package com.eden.alarmplus.logic

import android.content.Context
import com.eden.alarmplus.AlarmPlusApplication.Companion.DISMISS_WINDOW
import com.eden.alarmplus.data.AppContainer
import com.eden.alarmplus.logic.access.LocalStateHookProvider
import com.eden.alarmplus.logic.access.LogicListener
import com.eden.alarmplus.logic.access.StateHookProvider
import com.eden.alarmplus.logic.internal.StateProcessor
import com.eden.alarmplus.logic.internal.WakeScheduler

class LogicScope(
    private val context: Context,
    container: AppContainer,
    val listener: LogicListener?,
) {
    val stateProcessor: StateProcessor by lazy {
        StateProcessor(context, container.stateRepository, wakeScheduler, DISMISS_WINDOW)
    }

    val stateHookProvider: StateHookProvider by lazy {
        LocalStateHookProvider(context)
    }

    private val wakeScheduler: WakeScheduler by lazy {
        WakeScheduler(context, container.stateRepository, DISMISS_WINDOW)
    }
}