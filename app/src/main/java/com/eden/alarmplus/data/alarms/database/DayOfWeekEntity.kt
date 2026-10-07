package com.eden.alarmplus.data.alarms.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey
import com.eden.alarmplus.data.common.database.UuidLongs

@Entity(
    tableName = "day_of_weeks",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["group_upper_uuid", "group_lower_uuid"],
            childColumns = ["group_upper_uuid", "group_lower_uuid"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class DayOfWeekEntity(
    @PrimaryKey
    @Embedded(prefix = "group_")
    val groupUuid: UuidLongs,
    /**
     * See [com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags] for mask values
     */
    @ColumnInfo("mask")
    val mask: Int,
)