package com.eden.alarmplus.data.alarms.structure.dayofweek

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

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
    }

    fun any(flag: DayOfWeekFlags): Boolean =
        (mask and flag.mask) > 0
    
    fun all(flags: DayOfWeekFlags): Boolean =
        (mask and flags.mask) == flags.mask
}