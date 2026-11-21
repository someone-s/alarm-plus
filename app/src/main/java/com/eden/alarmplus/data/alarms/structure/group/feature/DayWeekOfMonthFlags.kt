package com.eden.alarmplus.data.alarms.structure.group.feature

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.sign


class DayWeekOfMonthFlagsSerializer : KSerializer<DayWeekOfMonthFlags> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(DayWeekOfMonthFlags::class.java.name, PrimitiveKind.STRING)


    override fun serialize(encoder: Encoder, value: DayWeekOfMonthFlags) {
        encoder.encodeInt(value.mask)
    }

    override fun deserialize(decoder: Decoder) = DayWeekOfMonthFlags(decoder.decodeInt())

}
@Immutable
@Parcelize
@SerialName("day-week-of-month-flags")
@Serializable(with = DayWeekOfMonthFlagsSerializer::class)
data class DayWeekOfMonthFlags(
    val mask: Int
) : Parcelable {
    companion object {
        val FIRST       = DayWeekOfMonthFlags(1 shl 0)
        val SECOND      = DayWeekOfMonthFlags(1 shl 1)
        val THIRD       = DayWeekOfMonthFlags(1 shl 2)
        val FORTH       = DayWeekOfMonthFlags(1 shl 3)
        val LAST        = DayWeekOfMonthFlags(1 shl 4)
        val PENULTIMATE = DayWeekOfMonthFlags(1 shl 5)
        val ALL_START   = merge(FIRST, SECOND, THIRD, FORTH)
        val ALL_END     = merge(LAST, PENULTIMATE)

        fun merge(vararg flags: DayWeekOfMonthFlags): DayWeekOfMonthFlags {
            var mergedMask = 0
            for ((mask) in flags) {
                mergedMask = mergedMask or mask
            }
            return DayWeekOfMonthFlags(mergedMask)
        }

    }

    fun any(flags: DayWeekOfMonthFlags): Boolean =
        (mask and flags.mask) > 0

    /**
     * @param dayWeekOfMonth 0 indexed
     */
    fun any(dayWeekOfMonth: Int, dayWeeksInMonth: Int): Boolean =
        (mask and ALL_START.mask and (1 shl dayWeekOfMonth)) or
        (mask and ALL_END.mask and (1 shl (4 + dayWeeksInMonth - dayWeekOfMonth - 1))) > 0

    data class MonthAndDayWeek(
        val monthOffset: Int,
        /**
         * must be >=0
         */
        val dayWeekOfMonth: Int,
    ): Comparable<MonthAndDayWeek> {

        override fun compareTo(other: MonthAndDayWeek): Int {
            val monthComparison = this.monthOffset - other.monthOffset
            if (monthComparison != 0) return monthComparison

            check(this.dayWeekOfMonth.sign == other.dayWeekOfMonth.sign) {
                "Cannot compare MonthAndDayWeek, both must use positive dayWeekIndex or negative dayWeekIndex."
            }
            return this.dayWeekOfMonth - other.dayWeekOfMonth
        }
    }

    /**
     * @param dayWeekOfMonth 0 indexed
     */
    fun nextOrSame(dayWeekOfMonth: Int, dayWeeksInMonth: Int, dayWeeksInNextMonth: Int): MonthAndDayWeek {

        val minStart = startDayWeeksOfMonth.minOf { start ->
            if (dayWeekOfMonth > start)
                MonthAndDayWeek(1, start)
            else
                MonthAndDayWeek(0, start)
        }

        val minEnd = endDayWeeksOfMonth.minOf { end ->
            val negative = dayWeekOfMonth - dayWeeksInMonth
            if (negative > end)
                MonthAndDayWeek(1, dayWeeksInNextMonth + end)
            else
                MonthAndDayWeek(0, dayWeeksInMonth + end)
        }

        return minOf(minStart, minEnd)
    }

    /**
     * @param dayWeekOfMonth 0 indexed
     */
    fun prevOrSame(dayWeekOfMonth: Int, dayWeeksInMonth: Int, dayWeeksInPrevMonth: Int): MonthAndDayWeek {

        val maxStart = startDayWeeksOfMonth.maxOf { start ->
            if (dayWeekOfMonth < start)
                MonthAndDayWeek(-1, start)
            else
                MonthAndDayWeek(0, start)
        }

        val maxEnd = endDayWeeksOfMonth.maxOf { end ->
            val negative = dayWeekOfMonth - dayWeeksInMonth
            if (negative < end)
                MonthAndDayWeek(-1, dayWeeksInPrevMonth + end)
            else
                MonthAndDayWeek(0, dayWeeksInMonth + end)
        }

        return maxOf(maxStart, maxEnd)
    }

    fun copyClear(flags: DayWeekOfMonthFlags): DayWeekOfMonthFlags =
        DayWeekOfMonthFlags(mask and (flags.mask.inv()))

    val startDayWeeksOfMonth: List<Int> get() = buildList {
        if ((mask and FIRST.mask) > 0)  add(0)
        if ((mask and SECOND.mask) > 0) add(1)
        if ((mask and THIRD.mask) > 0)  add(2)
        if ((mask and FORTH.mask) > 0)  add(3)
    }

    val endDayWeeksOfMonth: List<Int> get() = buildList {
        if ((mask and LAST.mask) > 0)        add(-1)
        if ((mask and PENULTIMATE.mask) > 0) add(-2)
    }

}