package com.eden.alarmplus.ui.alarms

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eden.alarmplus.MainApplication
import com.eden.alarmplus.ui.alarms.mainApplication

object GroupAlarmViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            GroupAlarmViewModel(mainApplication().container.groupAlarmsRepository)
        }
    }
}

fun CreationExtras.mainApplication(): MainApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MainApplication