package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.UserPreferencesEntity

@Dao
interface UserPreferencesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: UserPreferencesEntity)

    @Update
    suspend fun updatePreferences(preferences: UserPreferencesEntity)

    @Query("SELECT * FROM user_preferences WHERE userId = :userId LIMIT 1")
    suspend fun getPreferences(userId: String): UserPreferencesEntity?

    @Query("SELECT * FROM user_preferences WHERE userId = :userId LIMIT 1")
    fun getPreferencesFlow(userId: String): Flow<UserPreferencesEntity?>

    @Delete
    suspend fun deletePreferences(preferences: UserPreferencesEntity)

    @Query("DELETE FROM user_preferences WHERE userId = :userId")
    suspend fun deletePreferencesByUserId(userId: String)

    @Query("DELETE FROM user_preferences")
    suspend fun deleteAllPreferences()

    @Query("UPDATE user_preferences SET lastSyncTimestamp = :timestamp WHERE userId = :userId")
    suspend fun updateLastSyncTimestamp(userId: String, timestamp: Long)
}