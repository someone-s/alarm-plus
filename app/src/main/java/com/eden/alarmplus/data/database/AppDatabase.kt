package com.eden.alarmplus.data.database

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
    entities = [
        GroupEntity::class,
        AlarmEntity::class,
        StateEntity::class,
    ],
    version = 1,
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun groupAlarmsDao(): GroupAlarmsDao
    abstract fun stateDao(): StateDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app-database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}