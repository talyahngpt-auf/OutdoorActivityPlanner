package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao.ActivityDao
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao.WeatherCacheDao


@Database(
    entities = [ActivityEntity::class, WeatherCache::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {


    abstract fun activityDao(): ActivityDao
    abstract fun weatherCacheDao(): WeatherCacheDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "outdoor_activity_planner_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
