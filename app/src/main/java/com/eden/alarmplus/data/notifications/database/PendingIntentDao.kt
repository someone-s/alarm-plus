package com.eden.alarmplus.data.notifications.database


interface PendingIntentDao {
    suspend fun createRequestCode(customTag: Int, identifyingValue: String): Int

    suspend fun releaseRequestCode(customTag: Int, identifyingValue: String)
}