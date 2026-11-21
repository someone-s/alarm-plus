package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.DayOfWeekFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.RestrictionFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.ScheduleBeginFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.ScheduleEndFeature
import java.time.LocalDate
import kotlin.reflect.KClass

object RestrictionProviders {
    private val providers = mapOf<KClass<*>, RestrictionProvider>(
        DayOfWeekFeature::class to DayOfWeekProvider,
        ScheduleBeginFeature::class to ScheduleBeginProvider,
        ScheduleEndFeature::class to ScheduleEndProvider,
    )

    fun getScope(feature: RestrictionFeature): RestrictionScope {
        return providers[feature::class]!!.getScope(feature)
    }

    fun featuresHasInheritEnd(feature: RestrictionFeature): Boolean =
        getScope(feature).getEndDate() != null

    fun getScopes(features: Collection<RestrictionFeature>): List<RestrictionScope> =
        features.map { feature -> getScope(feature) }

    fun List<RestrictionScope>.isDateAllowed(date: LocalDate): Boolean =
        this.all { it.isDateAllowed(date) }

    data class StartDateResult(
        val hasRestriction: Boolean,
        val nextStartPeriod: StartPeriod?,
    )

    /**
     * If date is provided and in a valid period, a result with start date being date will be returned
     */
    fun List<RestrictionScope>.getFirstStartFrom(date: LocalDate? = null): StartDateResult {

        val inheritStartDate = this
            .mapNotNull { restriction -> restriction.getStartDate() }
            .maxByOrNull { startDate -> startDate }

        if (inheritStartDate == null)
            return StartDateResult(
                hasRestriction = false,
                nextStartPeriod = null,
            )

        val combinedStartDate =
            if (date != null) maxOf(inheritStartDate, date)
            else inheritStartDate

        var sorted = listOf(StartPeriod(on = combinedStartDate, until = null))

        for (restriction in this.sortedByDescending { it.filterOrder })
            sorted = restriction.filterStartDate(sorted)

        return StartDateResult(
            hasRestriction = true,
            nextStartPeriod = sorted.firstOrNull(),
        )
    }

    data class EndDateResult(
        val hasRestriction: Boolean,
        val lastEndPeriod: EndPeriod?,
    )

    /**
     * If date is provided and in a valid period, a result with end date being date will be returned
     */
    fun List<RestrictionScope>.getLastEndOn(date: LocalDate? = null): EndDateResult {

        val inheritEndDate = this
            .mapNotNull { restriction -> restriction.getEndDate() }
            .minByOrNull { endDate -> endDate }

        if (inheritEndDate == null)
            return EndDateResult(
                hasRestriction = false,
                lastEndPeriod = null,
            )

        val combinedEndDate =
            if (date != null) minOf(inheritEndDate, date)
            else inheritEndDate

        var sorted = listOf(EndPeriod(on = combinedEndDate, since = null))

        for ((index, restriction) in this.sortedByDescending { it.filterOrder }.withIndex())
            sorted = restriction.filterEndDate(sorted)

        return EndDateResult(
            hasRestriction = true,
            lastEndPeriod = sorted.lastOrNull(),
        )
    }
}