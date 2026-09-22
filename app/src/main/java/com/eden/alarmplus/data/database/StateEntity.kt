package com.eden.alarmplus.data.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

@Entity(
    tableName = "states",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["group_upper_uuid", "group_lower_uuid"],
            childColumns = ["group_upper_uuid", "group_lower_uuid"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class StateEntity(
    @PrimaryKey
    @Embedded(prefix = "group_")
    val groupUuid: UuidLongs,
    @ColumnInfo("value")
    val value: Int = UNDEFINED_VALUE,
) {
    companion object {
        const val UNDEFINED_VALUE = 0
        const val ACTIVE_VALUE = 1
        const val DISMISSED_VALUE = 2
        const val IDLE_VALUE = 3
    }
}