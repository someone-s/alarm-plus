package com.eden.alarmplus.data

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.eden.alarmplus.structure.group.GroupInfo

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
    fun toGroupInfo() = GroupInfo(
        uuid = uuid.toUuid(),
        name = name,
        active = active,
    )
}

fun GroupInfo.toGroupEntity() = GroupEntity(
    uuid = this.uuid.toUuidLongs(),
    name = this.name,
    active = this.active,
)