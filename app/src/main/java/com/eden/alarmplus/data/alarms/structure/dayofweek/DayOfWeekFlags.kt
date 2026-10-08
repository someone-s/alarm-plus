package com.eden.alarmplus.data.alarms.structure.dayofweek

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek

fun DayOfWeek.toFlags() = when (this) {
    DayOfWeek.MONDAY -> DayOfWeekFlags.MONDAY.mask
    DayOfWeek.TUESDAY -> DayOfWeekFlags.TUESDAY.mask
    DayOfWeek.WEDNESDAY -> DayOfWeekFlags.WEDNESDAY.mask
    DayOfWeek.THURSDAY -> DayOfWeekFlags.THURSDAY.mask
    DayOfWeek.FRIDAY -> DayOfWeekFlags.FRIDAY.mask
    DayOfWeek.SATURDAY -> DayOfWeekFlags.SATURDAY.mask
    DayOfWeek.SUNDAY -> DayOfWeekFlags.SUNDAY.mask
}

@Immutable
@Parcelize
data class DayOfWeekFlags(
    val mask: Int
) : Parcelable {
    companion object {
        val MONDAY = DayOfWeekFlags(0b1000000)
        val TUESDAY = DayOfWeekFlags(0b0100000)
        val WEDNESDAY = DayOfWeekFlags(0b0010000)
        val THURSDAY = DayOfWeekFlags(0b0001000)
        val FRIDAY = DayOfWeekFlags(0b0000100)
        val SATURDAY = DayOfWeekFlags(0b0000010)
        val SUNDAY = DayOfWeekFlags(0b0000001)
        val ALL = DayOfWeekFlags(0b1111111)

        fun merge(vararg flags: DayOfWeekFlags): DayOfWeekFlags {
            var mergedMask = 0
            for ((mask) in flags) {
                mergedMask = mergedMask or mask
            }
            return DayOfWeekFlags(mergedMask)
        }

        fun from(dayOfWeek: DayOfWeek): DayOfWeekFlags = when (dayOfWeek) {
            DayOfWeek.MONDAY -> MONDAY
            DayOfWeek.TUESDAY -> TUESDAY
            DayOfWeek.WEDNESDAY -> WEDNESDAY
            DayOfWeek.THURSDAY -> THURSDAY
            DayOfWeek.FRIDAY -> FRIDAY
            DayOfWeek.SATURDAY -> SATURDAY
            DayOfWeek.SUNDAY -> SUNDAY
        }
    }

    fun any(flags: DayOfWeekFlags): Boolean =
        (mask and flags.mask) > 0

    fun any(day: DayOfWeek): Boolean =
        (mask and from(day).mask) > 0
    
    fun all(flags: DayOfWeekFlags): Boolean =
        (mask and flags.mask) == flags.mask

    fun copyClear(flags: DayOfWeekFlags): DayOfWeekFlags =
        DayOfWeekFlags(mask and (flags.mask.inv()))

}