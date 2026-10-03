package com.eden.alarmplus.data.alarms.database

import androidx.room3.Embedded
import com.eden.alarmplus.data.common.database.UuidLongs

data class PrimedAtTimestamp(
    @Embedded(prefix = "group_")
    val groupUuid: UuidLongs
)