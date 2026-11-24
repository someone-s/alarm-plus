package com.eden.alarmplus.logic.internal.restrictions

import com.eden.alarmplus.data.alarms.structure.group.feature.DayWeekFromEndOfMonthFlags
import com.eden.alarmplus.data.alarms.structure.group.feature.DayWeekFromStartOfMonthFlags
import java.time.LocalDate

class DayWeekOfMonthScope(
    startFlags: DayWeekFromStartOfMonthFlags,
    endFlags: DayWeekFromEndOfMonthFlags,
): RestrictionScope {

    private val startLogic = DayWeekFromStartOfMonthLogic(startFlags)
    private val endLogic = DayWeekFromEndOfMonthLogic(endFlags)

    override val filterOrder = 900
    override val defaultFilterLimit = 6

    override fun isDateAllowed(date: LocalDate): Boolean =
        startLogic.isDateAllowed(date) || endLogic.isDateAllowed(date)

    override fun getStartDate() = null

    override fun filterStartDate(
        sorted: List<StartPeriod>,
        limit: Int?
    ): List<StartPeriod> {
        val bases = startLogic.filterStartDate(sorted, limit)
        val overlays = endLogic.filterStartDate(sorted, limit)

        val baseIterator = bases.iterator()
        fun getNextBase(): StartPeriod? = if (baseIterator.hasNext()) baseIterator.next() else null
        var currentBase = getNextBase()

        val overlayIterator = overlays.iterator()
        fun getNextOverlay(): StartPeriod? = if (overlayIterator.hasNext()) overlayIterator.next() else null
        var currentOverlay = getNextOverlay()

        var reachedInfinite = false

        val output = ArrayList<StartPeriod>(bases.size + overlays.size)

        while ((currentBase != null || currentOverlay != null) && !reachedInfinite) {

            if (currentBase == null) {
                checkNotNull(currentOverlay)
                output.add(currentOverlay)
                currentOverlay = getNextOverlay()
                continue
            }

            if (currentOverlay == null) {
                output.add(currentBase)
                currentBase = getNextBase()
                continue
            }


            if (currentBase.until == null || currentOverlay.until == null) {
                output.add(StartPeriod(
                    minOf(currentBase.on, currentOverlay.on),
                    null
                ))
                reachedInfinite = true
                continue
            }

            if (currentBase.until.isBefore(currentOverlay.on)) {
                output.add(currentBase)
                currentBase = getNextBase()
                continue
            }

            if (currentOverlay.until.isBefore(currentBase.on)) {
                output.add((currentOverlay))
                currentOverlay = getNextOverlay()
                continue
            }

            var combinedRange = StartPeriod(
                minOf(currentBase.on, currentOverlay.on),
                maxOf(currentBase.until, currentOverlay.until)
            )

            while (true) {

                var stable = true

                while (true) {

                    if (combinedRange.until == null)
                        break

                    val nextBase = getNextBase()

                    if (nextBase == null) {
                        currentBase = nextBase
                        break
                    }

                    if (nextBase.until == null) {
                        combinedRange = StartPeriod(combinedRange.on, null)
                        reachedInfinite = true
                        currentBase = nextBase
                        break
                    }

                    if (nextBase.on.isAfter(combinedRange.until)) {
                        currentBase = nextBase
                        break
                    }

                    combinedRange = StartPeriod(
                        combinedRange.on,
                        maxOf(combinedRange.until, nextBase.until)
                    )
                    currentBase = nextBase
                    stable = false
                }

                while (true) {

                    if (combinedRange.until == null)
                        break

                    val nextOverlay = getNextOverlay()

                    if (nextOverlay == null) {
                        currentOverlay = nextOverlay
                        break
                    }

                    if (nextOverlay.until == null) {
                        combinedRange = StartPeriod(combinedRange.on, null)
                        reachedInfinite = true
                        currentOverlay = nextOverlay
                        break
                    }

                    if (nextOverlay.on.isAfter(combinedRange.until)) {
                        currentOverlay = nextOverlay
                        break
                    }

                    combinedRange = StartPeriod(
                        combinedRange.on,
                        maxOf(combinedRange.until, nextOverlay.until)
                    )
                    currentOverlay = nextOverlay
                    stable = false
                }

                if (stable) break
            }

            output.add(combinedRange)
        }

        return output
    }

    override fun getEndDate() = null

    override fun filterEndDate(
        sorted: List<EndPeriod>,
        limit: Int?
    ): List<EndPeriod> {
        val bases = startLogic.filterEndDate(sorted, limit)
        val overlays = endLogic.filterEndDate(sorted, limit)

        val baseIterator = bases.iterator()
        fun getNextBase(): EndPeriod? = if (baseIterator.hasNext()) baseIterator.next() else null
        var currentBase = getNextBase()

        val overlayIterator = overlays.iterator()
        fun getNextOverlay(): EndPeriod? = if (overlayIterator.hasNext()) overlayIterator.next() else null
        var currentOverlay = getNextOverlay()

        var reachedInfinite = false

        val output = ArrayList<EndPeriod>(bases.size + overlays.size)

        while ((currentBase != null || currentOverlay != null) && !reachedInfinite) {

            if (currentBase == null) {
                checkNotNull(currentOverlay)
                output.add(currentOverlay)
                currentOverlay = getNextOverlay()
                continue
            }

            if (currentOverlay == null) {
                output.add(currentBase)
                currentBase = getNextBase()
                continue
            }


            if (currentBase.since == null || currentOverlay.since == null) {
                output.add(EndPeriod(
                    maxOf(currentBase.on, currentOverlay.on),
                    null
                ))
                reachedInfinite = true
                continue
            }

            if (currentBase.since.isAfter(currentOverlay.on)) {
                output.add(currentBase)
                currentBase = getNextBase()
                continue
            }

            if (currentOverlay.since.isAfter(currentBase.on)) {
                output.add((currentOverlay))
                currentOverlay = getNextOverlay()
                continue
            }

            var combinedRange = EndPeriod(
                maxOf(currentBase.on, currentOverlay.on),
                minOf(currentBase.since, currentOverlay.since)
            )

            while (true) {

                var stable = true

                while (true) {

                    if (combinedRange.since == null)
                        break

                    val nextBase = getNextBase()

                    if (nextBase == null) {
                        currentBase = nextBase
                        break
                    }

                    if (nextBase.since == null) {
                        combinedRange = EndPeriod(combinedRange.on, null)
                        reachedInfinite = true
                        currentBase = nextBase
                        break
                    }

                    if (nextBase.on.isBefore(combinedRange.since)) {
                        currentBase = nextBase
                        break
                    }

                    combinedRange = EndPeriod(
                        combinedRange.on,
                        minOf(combinedRange.since, nextBase.since)
                    )
                    currentBase = nextBase
                    stable = false
                }

                while (true) {

                    if (combinedRange.since == null)
                        break

                    val nextOverlay = getNextOverlay()

                    if (nextOverlay == null) {
                        currentOverlay = nextOverlay
                        break
                    }

                    if (nextOverlay.since == null) {
                        combinedRange = EndPeriod(combinedRange.on, null)
                        reachedInfinite = true
                        currentOverlay = nextOverlay
                        break
                    }

                    if (nextOverlay.on.isBefore(combinedRange.since)) {
                        currentOverlay = nextOverlay
                        break
                    }

                    combinedRange = EndPeriod(
                        combinedRange.on,
                        minOf(combinedRange.since, nextOverlay.since)
                    )
                    currentOverlay = nextOverlay
                    stable = false
                }

                if (stable) break
            }

            output.add(combinedRange)
        }

        return output
    }
}