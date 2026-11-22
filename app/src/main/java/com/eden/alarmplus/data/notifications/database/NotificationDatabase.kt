package com.eden.alarmplus.data.notifications.database

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [
        AlertEntity::class,
        ForewarnEntity::class,
    ],
    version = 2,
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
                    .addMigrations(
                        MIGRATION_1_2
                    )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            DROP INDEX IF EXISTS `index_alerts_group_upper_uuid_group_lower_uuid`
        """.trimIndent())
        connection.execSQL("""
            CREATE INDEX `index_alerts_group_upper_uuid_group_lower_uuid` ON `alerts` (`group_upper_uuid`, `group_lower_uuid`)
        """.trimIndent())
    }
}