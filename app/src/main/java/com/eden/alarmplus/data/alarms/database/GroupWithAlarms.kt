package com.eden.alarmplus.data.alarms.database

import androidx.room3.Embedded
import androidx.room3.Relation

data class GroupWithAlarms(
    @Embedded
    val group: GroupEntity,
    @Relation(
        parentColumns = ["group_upper_uuid", "group_lower_uuid"],
        entityColumns = ["group_upper_uuid", "group_lower_uuid"],
    )
    val unorderedAlarms: List<AlarmEntity>,
)