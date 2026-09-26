package com.eden.alarmplus.data.alarms.database

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
abstract class AlarmsDatabase: RoomDatabase() {
    protected abstract fun groupWithAlarmsInternalDao(): GroupsWithAlarmsInternalDao
    fun groupsWithAlarmsDao() = groupWithAlarmsInternalDao() as GroupsWithAlarmsDao
    abstract fun stateDao(): StateDao

    companion object {
        @Volatile
        private var Instance: AlarmsDatabase? = null

        fun getDatabase(context: Context): AlarmsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AlarmsDatabase::class.java, "alarms-database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}