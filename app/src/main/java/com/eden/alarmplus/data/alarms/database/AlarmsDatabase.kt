package com.eden.alarmplus.data.alarms.database

import android.content.Context
import androidx.room3.AutoMigration
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(
    entities = [
        GroupEntity::class,
        AlarmEntity::class,
        DayOfWeekEntity::class,
        StateEntity::class,
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(2, 3),
    ]
)
abstract class AlarmsDatabase: RoomDatabase() {
    protected abstract fun groupsInternalDao(): GroupsInternalDao
    fun groupsDao() = groupsInternalDao() as GroupsDao
    abstract fun stateDao(): StateDao

    companion object {
        @Volatile
        private var Instance: AlarmsDatabase? = null

        fun getDatabase(context: Context): AlarmsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AlarmsDatabase::class.java, "alarms-database")
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}