package com.eden.alarmplus.data.notifications.repository

import com.eden.alarmplus.data.notifications.database.PendingIntentDao

class ConcurrentPendingIntentRepository(private val pendingIntentDao: PendingIntentDao): PendingIntentRepository {

    override suspend fun createRequestCode(customTag: Int, identifyingValue: String): Int {
        return pendingIntentDao.createRequestCode(customTag, identifyingValue)
    }

    override suspend fun swapRequestCode(customTag: Int, identifyingValue: String): Int {
        releaseRequestCode(customTag, identifyingValue)
        return createRequestCode(customTag, identifyingValue)
    }

    override suspend fun releaseRequestCode(customTag: Int, identifyingValue: String) {
        pendingIntentDao.releaseRequestCode(customTag, identifyingValue)
    }
}