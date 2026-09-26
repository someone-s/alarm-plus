package com.eden.alarmplus.data.notifications.repository

interface PendingIntentRepository {

    suspend fun createRequestCode(customTag: Int, identifyingValue: String): Int

    suspend fun swapRequestCode(customTag: Int, identifyingValue: String): Int

    suspend fun releaseRequestCode(customTag: Int, identifyingValue: String)
}