package com.eden.alarmplus.data.alarms.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded

data class AlarmAtTimestamp(
    @Embedded
    val alarm: AlarmEntity,
    @ColumnInfo("is_last_in_group")
    val isLastInGroup: Boolean,
)