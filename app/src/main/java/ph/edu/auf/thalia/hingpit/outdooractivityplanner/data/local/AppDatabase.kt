package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao.ActivityDao
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao.UserPreferencesDao
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao.WeatherCacheDao


@Database(
    entities = [
        ActivityEntity::class,
        WeatherCache::class,
        UserPreferencesEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {


    abstract fun activityDao(): ActivityDao
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun userPreferencesDao(): UserPreferencesDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        // Migration from version 1 to 2: add time and weatherIconCode columns
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE activities ADD COLUMN time TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE activities ADD COLUMN weatherIconCode TEXT NOT NULL DEFAULT '01d'")
            }
        }


        // Migration from version 2 to 3: add user_preferences table
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                   CREATE TABLE IF NOT EXISTS user_preferences (
                       userId TEXT NOT NULL PRIMARY KEY,
                       favoriteActivityTypes TEXT NOT NULL DEFAULT '',
                       preferredWeatherConditions TEXT NOT NULL DEFAULT '',
                       preferredTimeOfDay TEXT NOT NULL DEFAULT '',
                       notificationsEnabled INTEGER NOT NULL DEFAULT 1,
                       syncEnabled INTEGER NOT NULL DEFAULT 1,
                       lastSyncTimestamp INTEGER NOT NULL DEFAULT 0,
                       createdAt INTEGER NOT NULL DEFAULT 0,
                       updatedAt INTEGER NOT NULL DEFAULT 0
                   )
               """.trimIndent())
            }
        }


        // Migration from version 3 to 4: add locationType and category columns
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE activities ADD COLUMN locationType TEXT NOT NULL DEFAULT 'Indoor'")
                database.execSQL("ALTER TABLE activities ADD COLUMN category TEXT NOT NULL DEFAULT 'General'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE activities ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "outdoor_activity_planner_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // include new migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
