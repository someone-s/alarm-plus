package com.eden.alarmplus.data.alarms.structure.dayofweek

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
import java.time.DayOfWeek

fun DayOfWeek.toFlags() = when (this) {
    DayOfWeek.MONDAY ->     DayOfWeekFlags.MONDAY.mask
    DayOfWeek.TUESDAY ->    DayOfWeekFlags.TUESDAY.mask
    DayOfWeek.WEDNESDAY ->  DayOfWeekFlags.WEDNESDAY.mask
    DayOfWeek.THURSDAY ->   DayOfWeekFlags.THURSDAY.mask
    DayOfWeek.FRIDAY ->     DayOfWeekFlags.FRIDAY.mask
    DayOfWeek.SATURDAY ->   DayOfWeekFlags.SATURDAY.mask
    DayOfWeek.SUNDAY ->     DayOfWeekFlags.SUNDAY.mask
}

class DayOfWeekFlagsSerializer : KSerializer<DayOfWeekFlags> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(DayOfWeekFlags::class.java.name, PrimitiveKind.STRING)


    override fun serialize(encoder: Encoder, value: DayOfWeekFlags) {
        encoder.encodeInt(value.mask)
    }

    override fun deserialize(decoder: Decoder) = DayOfWeekFlags(decoder.decodeInt())

}

@Immutable
@Parcelize
@SerialName("day-of-week-flags")
@Serializable(with = DayOfWeekFlagsSerializer::class)
data class DayOfWeekFlags(
    val mask: Int
) : Parcelable {
    companion object {
        val MONDAY =    DayOfWeekFlags(1 shl 6)
        val TUESDAY =   DayOfWeekFlags(1 shl 5)
        val WEDNESDAY = DayOfWeekFlags(1 shl 4)
        val THURSDAY =  DayOfWeekFlags(1 shl 3)
        val FRIDAY =    DayOfWeekFlags(1 shl 2)
        val SATURDAY =  DayOfWeekFlags(1 shl 1)
        val SUNDAY =    DayOfWeekFlags(1 shl 0)
        val ALL =       merge(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)

        fun merge(vararg flags: DayOfWeekFlags): DayOfWeekFlags {
            var mergedMask = 0
            for ((mask) in flags) {
                mergedMask = mergedMask or mask
            }
            return DayOfWeekFlags(mergedMask)
        }

        fun from(dayOfWeek: DayOfWeek): DayOfWeekFlags = when (dayOfWeek) {
            DayOfWeek.MONDAY ->     MONDAY
            DayOfWeek.TUESDAY ->    TUESDAY
            DayOfWeek.WEDNESDAY ->  WEDNESDAY
            DayOfWeek.THURSDAY ->   THURSDAY
            DayOfWeek.FRIDAY ->     FRIDAY
            DayOfWeek.SATURDAY ->   SATURDAY
            DayOfWeek.SUNDAY ->     SUNDAY
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

    fun toDayOfWeeks(): List<DayOfWeek> = buildList {
        if ((mask and MONDAY.mask) > 0)     add(DayOfWeek.MONDAY)
        if ((mask and TUESDAY.mask) > 0)    add(DayOfWeek.TUESDAY)
        if ((mask and WEDNESDAY.mask) > 0)  add(DayOfWeek.WEDNESDAY)
        if ((mask and THURSDAY.mask) > 0)   add(DayOfWeek.THURSDAY)
        if ((mask and FRIDAY.mask) > 0)     add(DayOfWeek.FRIDAY)
        if ((mask and SATURDAY.mask) > 0)   add(DayOfWeek.SATURDAY)
        if ((mask and SUNDAY.mask) > 0)     add(DayOfWeek.SUNDAY)
    }

}