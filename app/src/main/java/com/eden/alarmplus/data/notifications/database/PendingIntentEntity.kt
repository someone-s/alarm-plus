package com.eden.alarmplus.data.notifications.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity

/**
 * This table exist to prevent colliding [requestCode] when multiple [android.app.PendingIntent]
 * have the same [android.content.Intent.filterEquals] due to only difference being in [android.content.Intent.putExtra]
 *
 * [customTag] is intended to be unique per [android.content.Intent] type,
 * i.e.  1 for AReceiver and 2 for BReceiver
 *
 * [identifyingValue] is used to look up [requestCode] in conjunction with [customTag],
 * this is typically what is in [android.content.Intent.putExtra]
 */
@Entity(
    tableName = "pending_intents",
    primaryKeys = [
        "custom_tag",
        "request_code",
    ]
)
data class PendingIntentEntity(
    @ColumnInfo("custom_tag")
    val customTag: Int,
    @ColumnInfo("request_code")
    val requestCode: Int,
    @ColumnInfo("identifying_value")
    val identifyingValue: String,
)