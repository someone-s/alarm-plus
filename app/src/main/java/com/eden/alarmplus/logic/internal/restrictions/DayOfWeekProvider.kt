package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.DayOfWeekFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.RestrictionFeature

object DayOfWeekProvider: RestrictionProvider {
    override fun getScope(feature: RestrictionFeature): RestrictionScope {
        check(feature is DayOfWeekFeature)
        return DayOfWeekScope(feature.flags)
    }
}