package com.eden.alarmplus.ui.main.repeats

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eden.alarmplus.AlarmPlusApplication

object RepeatsViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = mainApplication()
            val container = application.container
            RepeatsViewModel(
                container.groupAlarmsRepository,
                container.timeTickRepository,
                application.logicScope.stateHookProvider,
            )
        }
    }
}

private fun CreationExtras.mainApplication(): AlarmPlusApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AlarmPlusApplication