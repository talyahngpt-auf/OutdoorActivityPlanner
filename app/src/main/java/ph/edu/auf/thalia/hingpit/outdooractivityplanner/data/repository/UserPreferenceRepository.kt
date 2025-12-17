package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.UserPreferences
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao.UserPreferencesDao
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.toPreferences


class UserPreferencesRepository(
    private val userPreferencesDao: UserPreferencesDao
) {


    suspend fun insertPreferences(preferences: UserPreferences) {
        userPreferencesDao.insertPreferences(preferences.toEntity())
    }


    suspend fun updatePreferences(preferences: UserPreferences) {
        userPreferencesDao.updatePreferences(preferences.toEntity())
    }


    suspend fun getPreferences(userId: String): UserPreferences? {
        return userPreferencesDao.getPreferences(userId)?.toPreferences()
    }


    fun getPreferencesFlow(userId: String): Flow<UserPreferences?> {
        return userPreferencesDao.getPreferencesFlow(userId).map { entity ->
            entity?.toPreferences()
        }
    }


    suspend fun deletePreferences(userId: String) {
        userPreferencesDao.deletePreferencesByUserId(userId)
    }


    suspend fun deleteAllPreferences() {
        userPreferencesDao.deleteAllPreferences()
    }


    suspend fun updateLastSyncTimestamp(userId: String, timestamp: Long) {
        userPreferencesDao.updateLastSyncTimestamp(userId, timestamp)
    }


    suspend fun createDefaultPreferences(userId: String): UserPreferences {
        val defaultPreferences = UserPreferences(
            userId = userId,
            favoriteActivityTypes = listOf("outdoor", "sports"),
            preferredWeatherConditions = listOf("sunny", "cloudy"),
            preferredTimeOfDay = listOf("morning", "afternoon"),
            notificationsEnabled = true,
            syncEnabled = true,
            lastSyncTimestamp = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        insertPreferences(defaultPreferences)
        return defaultPreferences
    }
}
