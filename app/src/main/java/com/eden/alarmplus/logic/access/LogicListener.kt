package com.eden.alarmplus.logic.access

import android.content.Context
import kotlin.uuid.Uuid

interface LogicListener {
    fun onInitialize(context: Context)
    fun onDismiss(context: Context, groupUuid: Uuid)
}