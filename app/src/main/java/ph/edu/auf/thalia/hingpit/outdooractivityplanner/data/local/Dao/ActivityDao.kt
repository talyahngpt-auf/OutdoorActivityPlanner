package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao


import androidx.room.*
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity


@Dao
interface ActivityDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityEntity>)


    @Update
    suspend fun updateActivity(activity: ActivityEntity)


    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)


    @Query("SELECT * FROM activities ORDER BY createdAt DESC")
    suspend fun getAllActivities(): List<ActivityEntity>


    @Query("SELECT * FROM activities WHERE date = :date ORDER BY createdAt DESC")
    suspend fun getActivitiesByDate(date: String): List<ActivityEntity>


    @Query("SELECT * FROM activities WHERE isCompleted = 0 ORDER BY createdAt DESC")
    suspend fun getPendingActivities(): List<ActivityEntity>


    @Query("SELECT * FROM activities WHERE isCompleted = 1 ORDER BY createdAt DESC")
    suspend fun getCompletedActivities(): List<ActivityEntity>
}
