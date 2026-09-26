package com.eden.alarmplus.data.common.repository

import com.eden.alarmplus.data.common.database.UuidLongs
import kotlin.uuid.Uuid

fun UuidLongs.toUuid() = Uuid.fromLongs(this.upperUuid, this.lowerUuid)

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