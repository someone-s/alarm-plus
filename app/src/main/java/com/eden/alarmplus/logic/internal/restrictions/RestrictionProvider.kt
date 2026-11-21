package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.RestrictionFeature

interface RestrictionProvider {
    fun getScope(feature: RestrictionFeature): RestrictionScope
}