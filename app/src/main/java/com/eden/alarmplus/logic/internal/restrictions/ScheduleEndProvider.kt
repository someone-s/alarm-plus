package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.RestrictionFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.ScheduleEndFeature

object ScheduleEndProvider: RestrictionProvider {
    override fun getScope(feature: RestrictionFeature): RestrictionScope {
        check(feature is ScheduleEndFeature)
        return ScheduleEndScope(feature.date)
    }
}