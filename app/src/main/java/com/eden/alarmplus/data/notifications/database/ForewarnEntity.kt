package com.eden.alarmplus.data.notifications.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.eden.alarmplus.data.common.database.UuidLongs

@Entity(
    tableName = "forewarns",
)
data class ForewarnEntity(
    @PrimaryKey
    @Embedded(prefix = "group_")
    val groupUuid: UuidLongs,
    @ColumnInfo(name = "notification_id")
    val notificationId: Int,
)