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

    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllActivities(userId: String): List<ActivityEntity>

    @Query("SELECT * FROM activities WHERE userId = :userId AND date = :date ORDER BY createdAt DESC")
    suspend fun getActivitiesByDate(userId: String, date: String): List<ActivityEntity>

    @Query("SELECT * FROM activities WHERE userId = :userId AND isCompleted = 0 ORDER BY createdAt DESC")
    suspend fun getPendingActivities(userId: String): List<ActivityEntity>

    @Query("SELECT * FROM activities WHERE userId = :userId AND isCompleted = 1 ORDER BY createdAt DESC")
    suspend fun getCompletedActivities(userId: String): List<ActivityEntity>

    @Query("DELETE FROM activities WHERE userId = :userId")
    suspend fun deleteAllActivitiesByUser(userId: String)
}
