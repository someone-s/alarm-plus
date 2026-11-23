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


class DayWeekFromStartOfMonthFlagsSerializer : KSerializer<DayWeekFromStartOfMonthFlags> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(DayWeekFromStartOfMonthFlags::class.java.name, PrimitiveKind.STRING)


    override fun serialize(encoder: Encoder, value: DayWeekFromStartOfMonthFlags) {
        encoder.encodeInt(value.mask)
    }

    override fun deserialize(decoder: Decoder) = DayWeekFromStartOfMonthFlags(decoder.decodeInt())

}
@Immutable
@Parcelize
@SerialName("day-week-from-start-of-month-flags")
@Serializable(with = DayWeekFromStartOfMonthFlagsSerializer::class)
data class DayWeekFromStartOfMonthFlags(
    val mask: Int
) : Parcelable {
    companion object {
        val FIRST   = DayWeekFromStartOfMonthFlags(1 shl 0)
        val SECOND  = DayWeekFromStartOfMonthFlags(1 shl 1)
        val THIRD   = DayWeekFromStartOfMonthFlags(1 shl 2)
        val FORTH   = DayWeekFromStartOfMonthFlags(1 shl 3)
        val ALL     = merge(FIRST, SECOND, THIRD, FORTH)

        fun merge(vararg flags: DayWeekFromStartOfMonthFlags): DayWeekFromStartOfMonthFlags {
            var mergedMask = 0
            for ((mask) in flags) {
                mergedMask = mergedMask or mask
            }
            return DayWeekFromStartOfMonthFlags(mergedMask)
        }

    }

    fun any(flags: DayWeekFromStartOfMonthFlags): Boolean =
        (mask and flags.mask) > 0

    /**
     * @param dayWeekFromStartOfMonth 0 indexed
     */
    fun any(dayWeekFromStartOfMonth: Int): Boolean =
        (mask and ALL.mask and (1 shl dayWeekFromStartOfMonth)) > 0

    fun all(flags: DayWeekFromStartOfMonthFlags): Boolean =
        (mask and flags.mask) == flags.mask

    data class MonthAndDayWeekFromStart(
        val monthOffset: Int,
        /**
         * must be >=0
         */
        val dayWeekFromStartOfMonth: Int,
    ): Comparable<MonthAndDayWeekFromStart> {

        override fun compareTo(other: MonthAndDayWeekFromStart): Int {
            val monthComparison = this.monthOffset - other.monthOffset
            if (monthComparison != 0) return monthComparison
            return this.dayWeekFromStartOfMonth - other.dayWeekFromStartOfMonth
        }
    }

    /**
     * @param dayWeekFromStartOfMonth 0 indexed
     */
    fun nextOrSame(dayWeekFromStartOfMonth: Int): MonthAndDayWeekFromStart =
        startDayWeeksOfMonth.minOf { start ->
            MonthAndDayWeekFromStart(
                if (dayWeekFromStartOfMonth > start) 1 else 0,
                start
            )
        }

    /**
     * @param dayWeekFromStartOfMonth 0 indexed
     */
    fun prevOrSame(dayWeekFromStartOfMonth: Int): MonthAndDayWeekFromStart =
        startDayWeeksOfMonth.maxOf { start ->
            MonthAndDayWeekFromStart(
                if (dayWeekFromStartOfMonth < start) -1 else 0,
                start
            )
        }

    fun copyClear(flags: DayWeekFromStartOfMonthFlags): DayWeekFromStartOfMonthFlags =
        DayWeekFromStartOfMonthFlags(mask and (flags.mask.inv()))

    val startDayWeeksOfMonth: List<Int> get() = buildList {
        if ((mask and FIRST.mask) > 0)  add(0)
        if ((mask and SECOND.mask) > 0) add(1)
        if ((mask and THIRD.mask) > 0)  add(2)
        if ((mask and FORTH.mask) > 0)  add(3)
    }

    fun split(): List<DayWeekFromStartOfMonthFlags> = buildList {
        if ((mask and FIRST.mask) > 0)  add(FIRST)
        if ((mask and SECOND.mask) > 0) add(SECOND)
        if ((mask and THIRD.mask) > 0)  add(THIRD)
        if ((mask and FORTH.mask) > 0)  add(FORTH)
    }

}