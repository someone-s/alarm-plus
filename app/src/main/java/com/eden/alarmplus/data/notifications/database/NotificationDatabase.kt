package com.eden.alarmplus.data.notifications.database

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
    entities = [
        AlertEntity::class,
        ForewarnEntity::class,
    ],
    version = 1,
)
abstract class NotificationDatabase: RoomDatabase() {
    protected abstract fun notificationInternalDao(): NotificationInternalDao
    fun notificationDao(): NotificationDao = notificationInternalDao()

    companion object {
        @Volatile
        private var Instance: NotificationDatabase? = null

        fun getDatabase(context: Context): NotificationDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, NotificationDatabase::class.java, "notifications-database")
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}