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
abstract class GroupAlarmsDatabase: RoomDatabase() {
    abstract fun groupAlarmsDao(): GroupAlarmsDao

    companion object {
        @Volatile
        private var Instance: GroupAlarmsDatabase? = null

        fun getDatabase(context: Context): GroupAlarmsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, GroupAlarmsDatabase::class.java, "groupalarm_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}