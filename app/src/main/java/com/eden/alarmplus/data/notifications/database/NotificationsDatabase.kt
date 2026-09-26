package com.eden.alarmplus.data.notifications.database

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
    entities = [
        PendingIntentEntity::class,
    ],
    version = 1,
)
abstract class NotificationsDatabase: RoomDatabase() {

    protected abstract fun pendingIntentInternalDao(): PendingIntentInternalDao

    fun pendingIntentDao() = pendingIntentInternalDao() as PendingIntentDao

    companion object {
        @Volatile
        private var Instance: NotificationsDatabase? = null

        fun getDatabase(context: Context): NotificationsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, NotificationsDatabase::class.java, "notifications-database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}