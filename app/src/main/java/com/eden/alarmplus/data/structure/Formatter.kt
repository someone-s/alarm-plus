package com.eden.alarmplus.data.structure

import com.eden.alarmplus.data.structure.alarm.AlarmFeature
import com.eden.alarmplus.data.structure.alarm.feature.AudioFeature
import com.eden.alarmplus.data.structure.alarm.feature.VibrateFeature
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


val formatter = Json {
    serializersModule = SerializersModule {
        polymorphic(AlarmFeature::class) {
            subclass(AudioFeature::class)
            subclass(VibrateFeature::class)
        }
    }
}