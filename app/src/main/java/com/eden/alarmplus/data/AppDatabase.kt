package com.eden.alarmplus.data

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [
        GroupEntity::class,
        AlarmEntity::class,
    ],
    version = 0,
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun appDao(): AppDao
}