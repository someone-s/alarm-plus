package com.eden.alarmplus.data.notifications.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.eden.alarmplus.data.common.database.UuidLongs

@Entity(
    tableName = "alerts",
    indices = [
        Index("group_upper_uuid", "group_lower_uuid", unique = true)
    ]
)
data class AlertEntity(
    @PrimaryKey
    val index: Int = -1,
    @Embedded(prefix = "group_")
    val groupUuid: UuidLongs,
    @ColumnInfo(name = "notification_id")
    val notificationId: Int,
    @ColumnInfo(name = "displayed_name")
    val displayedName: String?,
    /**
     * In minutes
     */
    @ColumnInfo(name = "displayed_timestamp")
    val displayedTimestamp: Int?,
    @ColumnInfo(name = "snooze_allowed")
    val snoozeAllowed: Boolean,
    @ColumnInfo(name = "dismiss_allowed")
    val dismissAllowed: Boolean,
    @ColumnInfo(name = "audio_uri")
    val audioUriString: String?,
    @ColumnInfo(name = "vibrate_set")
    val vibrateEnabled: Boolean,
) {
    fun shallowCopy(index: Int) = AlertEntity(
        index = index,
        groupUuid = this.groupUuid,
        notificationId = this.notificationId,
        displayedName = this.displayedName,
        displayedTimestamp = this.displayedTimestamp,
        snoozeAllowed = this.snoozeAllowed,
        dismissAllowed = this.dismissAllowed,
        audioUriString = this.audioUriString,
        vibrateEnabled = this.vibrateEnabled,
    )
}