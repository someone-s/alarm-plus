package com.eden.alarmplus.data.alarms.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Relation

data class AlarmWithGroup(
    @Embedded
    val alarm: AlarmEntity,
    @Relation(
        parentColumns = ["group_upper_uuid", "group_lower_uuid"],
        entityColumns = ["group_upper_uuid", "group_lower_uuid"],
    )
    val group: GroupEntity,
    @Relation(
        parentColumns = ["group_upper_uuid", "group_lower_uuid"],
        entityColumns = ["group_upper_uuid", "group_lower_uuid"],
    )
    val dayOfWeekEntity: DayOfWeekEntity?,
)