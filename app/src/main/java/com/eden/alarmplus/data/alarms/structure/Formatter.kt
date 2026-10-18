package com.eden.alarmplus.data.alarms.structure

import com.eden.alarmplus.data.alarms.structure.alarm.AlarmFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.AudioFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.PinStartFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.VibrateFeature
import com.eden.alarmplus.data.alarms.structure.group.GroupFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.DayOfWeekFeature
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


val formatter = Json {
    serializersModule = SerializersModule {
        polymorphic(GroupFeature::class) {
            subclass(DayOfWeekFeature::class)
        }
        polymorphic(AlarmFeature::class) {
            subclass(AudioFeature::class)
            subclass(VibrateFeature::class)
            subclass(PinStartFeature::class)
        }
    }
}