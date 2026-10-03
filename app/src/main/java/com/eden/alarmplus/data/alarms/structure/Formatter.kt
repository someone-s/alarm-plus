package com.eden.alarmplus.data.alarms.structure

import com.eden.alarmplus.data.alarms.structure.alarm.AlarmFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.AudioFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.PinStartFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.VibrateFeature
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


val formatter = Json {
    serializersModule = SerializersModule {
        polymorphic(AlarmFeature::class) {
            subclass(AudioFeature::class)
            subclass(VibrateFeature::class)
            subclass(PinStartFeature::class)
        }
    }
}