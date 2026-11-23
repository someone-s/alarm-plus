package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.DayOfWeekFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.DayWeekOfMonthFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.RestrictionFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.ScheduleBeginFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.ScheduleEndFeature
import java.time.LocalDate
import kotlin.reflect.KClass

object RestrictionProviders {
    private val providers = mapOf<KClass<*>, RestrictionProvider>(
        DayOfWeekFeature::class to DayOfWeekProvider,
        DayWeekOfMonthFeature::class to DayWeekOfMonthProvider,
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
        val hasFixedRestriction: Boolean,
        val nextStartPeriod: StartPeriod?,
    )

    /**
     * If date is provided and in a valid period, a result with start date being date will be returned
     */
    fun List<RestrictionScope>.getFirstStartFrom(date: LocalDate? = null): StartDateResult {

        val inheritStartDate = this
            .mapNotNull { restriction -> restriction.getStartDate() }
            .maxByOrNull { startDate -> startDate }

        val combinedStartDate =
            if (date != null && inheritStartDate != null) maxOf(inheritStartDate, date)
            else inheritStartDate ?: date ?: LocalDate.now()

        var sorted = listOf(StartPeriod(on = combinedStartDate, until = null))

        for (restriction in this.sortedBy { it.filterOrder })
            sorted = restriction.filterStartDate(sorted)

        return StartDateResult(
            hasFixedRestriction = inheritStartDate != null,
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

        val combinedEndDate =
            if (date != null && inheritEndDate != null) minOf(inheritEndDate, date)
            else inheritEndDate ?: date ?: LocalDate.now()

        var sorted = listOf(EndPeriod(on = combinedEndDate, since = null))

        for (restriction in this.sortedBy { it.filterOrder })
            sorted = restriction.filterEndDate(sorted)

        return EndDateResult(
            hasRestriction = inheritEndDate != null,
            lastEndPeriod = sorted.lastOrNull(),
        )
    }
}