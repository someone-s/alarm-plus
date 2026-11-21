package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.RestrictionFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.ScheduleBeginFeature

object ScheduleBeginProvider: RestrictionProvider {
    override fun getScope(feature: RestrictionFeature): RestrictionScope {
        check(feature is ScheduleBeginFeature)
        return ScheduleBeginScope(feature.date)
    }
}