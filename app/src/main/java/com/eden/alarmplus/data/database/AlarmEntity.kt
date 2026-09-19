package com.eden.alarmplus.data.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "alarms",
    indices = [
        Index(
            "alarm_upper_uuid", "alarm_lower_uuid",
            "group_upper_uuid", "group_lower_uuid",
            unique = true,
        ),
    ],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["group_upper_uuid", "group_lower_uuid"],
            childColumns = ["group_upper_uuid", "group_lower_uuid"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class AlarmEntity(
    @PrimaryKey
    @Embedded(prefix = "alarm_")
    val alarmUuid: UuidLongs,
    @Embedded(prefix = "group_")
    val groupUuid: UuidLongs,
    @ColumnInfo(name = "timestamp")
    val timestamp: Int,
    @ColumnInfo(name = "features")
    val features: String,
)