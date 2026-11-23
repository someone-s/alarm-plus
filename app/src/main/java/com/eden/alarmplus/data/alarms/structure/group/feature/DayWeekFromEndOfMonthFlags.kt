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


class DayWeekFromEndOfMonthFlagsSerializer : KSerializer<DayWeekFromEndOfMonthFlags> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(DayWeekFromEndOfMonthFlags::class.java.name, PrimitiveKind.STRING)


    override fun serialize(encoder: Encoder, value: DayWeekFromEndOfMonthFlags) {
        encoder.encodeInt(value.mask)
    }

    override fun deserialize(decoder: Decoder) = DayWeekFromEndOfMonthFlags(decoder.decodeInt())

}
@Immutable
@Parcelize
@SerialName("day-week-from-end-of-month-flags")
@Serializable(with = DayWeekFromEndOfMonthFlagsSerializer::class)
data class DayWeekFromEndOfMonthFlags(
    val mask: Int
) : Parcelable {
    companion object {
        val LAST        = DayWeekFromEndOfMonthFlags(1 shl 0)
        val PENULTIMATE = DayWeekFromEndOfMonthFlags(1 shl 1)
        val ALL         = merge(LAST, PENULTIMATE)

        fun merge(vararg flags: DayWeekFromEndOfMonthFlags): DayWeekFromEndOfMonthFlags {
            var mergedMask = 0
            for ((mask) in flags) {
                mergedMask = mergedMask or mask
            }
            return DayWeekFromEndOfMonthFlags(mergedMask)
        }

    }

    fun any(flags: DayWeekFromEndOfMonthFlags): Boolean =
        (mask and flags.mask) > 0

    /**
     * @param dayWeekFromEndOfMonth 0 indexed
     */
    fun any(dayWeekFromEndOfMonth: Int): Boolean =
        (mask and ALL.mask and (1 shl dayWeekFromEndOfMonth)) > 0

    fun all(flags: DayWeekFromEndOfMonthFlags): Boolean =
        (mask and flags.mask) == flags.mask

    data class MonthAndDayWeekFromEnd(
        val monthOffset: Int,
        /**
         * must be >=0
         */
        val dayWeekFromEndOfMonth: Int,
    ): Comparable<MonthAndDayWeekFromEnd> {

        override fun compareTo(other: MonthAndDayWeekFromEnd): Int {
            val monthComparison = this.monthOffset - other.monthOffset
            if (monthComparison != 0) return monthComparison
            return other.dayWeekFromEndOfMonth - this.dayWeekFromEndOfMonth
        }
    }

    /**
     * @param dayWeekFromEndOfMonth 0 indexed
     */
    fun nextOrSame(dayWeekFromEndOfMonth: Int): MonthAndDayWeekFromEnd =
        endDayWeeksOfMonth.minOf { end ->
            MonthAndDayWeekFromEnd(
                if (dayWeekFromEndOfMonth < end) 1 else 0,
                end
            )
        }

    /**
     * @param dayWeekFromEndOfMonth 0 indexed
     */
    fun prevOrSame(dayWeekFromEndOfMonth: Int): MonthAndDayWeekFromEnd =
        endDayWeeksOfMonth.maxOf { end ->
            MonthAndDayWeekFromEnd(
                if (dayWeekFromEndOfMonth > end) -1 else 0,
                end
            )
        }

    fun copyClear(flags: DayWeekFromEndOfMonthFlags): DayWeekFromEndOfMonthFlags =
        DayWeekFromEndOfMonthFlags(mask and (flags.mask.inv()))

    val endDayWeeksOfMonth: List<Int> get() = buildList {
        if ((mask and LAST.mask) > 0)        add(0)
        if ((mask and PENULTIMATE.mask) > 0) add(1)
    }

    fun split(): List<DayWeekFromEndOfMonthFlags> = buildList {
        if ((mask and LAST.mask) > 0)        add(LAST)
        if ((mask and PENULTIMATE.mask) > 0) add(PENULTIMATE)
    }

}