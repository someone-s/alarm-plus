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
    @ColumnInfo(name = "features")
    val features: String,
    @ColumnInfo("state")
    var state: Int = ENABLED_UNDEFINED_DEFAULT_VALUE,
) {
    companion object {
        const val DISABLED_VALUE = 6
        const val ENABLED_UNDEFINED_MANUAL_VALUE = 7
        const val ENABLED_UNDEFINED_DEFAULT_VALUE = 0
        const val ENABLED_PENDING_VALUE = 5
        const val ENABLED_PRIMED_VALUE = 4
        const val ENABLED_ACTIVE_VALUE = 1
        const val ENABLED_DISMISSED_VALUE = 2
        const val ENABLED_IDLE_VALUE = 3
    }
}