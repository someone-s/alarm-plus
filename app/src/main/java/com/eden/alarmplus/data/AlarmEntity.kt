package com.eden.alarmplus.data

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.eden.alarmplus.structure.alarm.AlarmData
import com.eden.alarmplus.structure.alarm.AlarmFeature
import com.eden.alarmplus.structure.alarm.AlarmInfo
import com.eden.alarmplus.structure.formatter
import java.time.LocalTime
import kotlin.uuid.Uuid

@Entity(
    tableName = "alarms",
    indices = [
        Index(
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
) {
    fun toAlarmData(): AlarmData {
        val time = LocalTime.of(timestamp / 60, timestamp % 60)

        val features: List<AlarmFeature> = formatter.decodeFromString(features)

        return AlarmData(
            info = AlarmInfo(
                uuid = alarmUuid.toUuid(),
                time = time,
            ),
            features = features,
        )
    }

    fun getGroup() = groupUuid.toUuid()
}

fun AlarmData.toAlarmEntity(group: Uuid): AlarmEntity {

    val timestamp = this.info.time.let { it.hour * 60 + it.minute }

    val features = formatter.encodeToString(this.features)

    return AlarmEntity(
        alarmUuid = this.info.uuid.toUuidLongs(),
        groupUuid = group.toUuidLongs(),
        timestamp = timestamp,
        features = features,
    )
}