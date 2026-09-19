package com.eden.alarmplus.data.database

import androidx.room3.ColumnInfo

data class UuidLongs(
    @ColumnInfo("upper_uuid")
    val upperUuid: Long,
    @ColumnInfo("lower_uuid")
    val lowerUuid: Long,
) {
}
