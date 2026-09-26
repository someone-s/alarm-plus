package com.eden.alarmplus.data.alarms.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(
    tableName = "groups",
)
data class GroupEntity(
    @PrimaryKey
    @Embedded(prefix = "group_")
    val uuid: UuidLongs,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "active")
    val active: Boolean,
) {
}