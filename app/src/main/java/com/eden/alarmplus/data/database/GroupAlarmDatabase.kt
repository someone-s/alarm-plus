package com.eden.alarmplus.data.database

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
    entities = [
        GroupEntity::class,
        AlarmEntity::class,
    ],
    version = 1,
)
abstract class GroupAlarmDatabase: RoomDatabase() {
    abstract fun groupAlarmDao(): GroupAlarmDao

    companion object {
        @Volatile
        private var Instance: GroupAlarmDatabase? = null

        fun getDatabase(context: Context): GroupAlarmDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, GroupAlarmDatabase::class.java, "groupalarm_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}