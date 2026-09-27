package com.eden.alarmplus.data.alarms.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.eden.alarmplus.data.common.database.UuidLongs

@Entity(
    tableName = "groups",
)
data class GroupEntity(
    @PrimaryKey
    @Embedded(prefix = "group_")
    val uuid: UuidLongs,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "enabled")
    val enabled: Boolean,
) {
}