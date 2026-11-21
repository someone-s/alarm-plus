package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.DayWeekOfMonthFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.RestrictionFeature

object DayWeekOfMonthProvider: RestrictionProvider {
    override fun getScope(feature: RestrictionFeature): RestrictionScope {
        check(feature is DayWeekOfMonthFeature)
        return DayWeekOfMonthScope(feature.flags)
    }
}