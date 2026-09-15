package com.eden.alarmplus.data

import androidx.room3.ColumnInfo
import kotlin.uuid.Uuid

data class UuidLongs(
    @ColumnInfo("upper_uuid")
    val upperUuid: Long,
    @ColumnInfo("lower_uuid")
    val lowerUuid: Long,
) {
    fun toUuid() = Uuid.fromLongs(upperUuid, lowerUuid)
}

fun Uuid.toUuidLongs(): UuidLongs {
    val upperUuid: Long
    val lowerUuid: Long
    this.toLongs { mostSignificantBits, leastSignificantBits ->
        upperUuid = mostSignificantBits
        lowerUuid = leastSignificantBits
    }

    return UuidLongs(
        upperUuid = upperUuid,
        lowerUuid = lowerUuid,
    )
}
