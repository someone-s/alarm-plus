package com.eden.alarmplus.main

import android.app.Application
import com.eden.alarmplus.data.container.AppContainer
import com.eden.alarmplus.data.container.AppDataContainer

class MainApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}