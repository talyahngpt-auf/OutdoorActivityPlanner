package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao.ActivityDao
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao.WeatherCacheDao

@Database(
    entities = [ActivityEntity::class, WeatherCache::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun activityDao(): ActivityDao
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2 (adds time column)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE activities ADD COLUMN time TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "outdoor_activity_planner_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}