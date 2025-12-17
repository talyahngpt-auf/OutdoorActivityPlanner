package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val userId: String = "",
    val favoriteActivityTypes: String = "",
    val preferredWeatherConditions: String = "",
    val preferredTimeOfDay: String = "",
    val notificationsEnabled: Boolean = true,
    val syncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


data class UserPreferences(
    val userId: String = "",
    val favoriteActivityTypes: List<String> = emptyList(),
    val preferredWeatherConditions: List<String> = emptyList(),
    val preferredTimeOfDay: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val syncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): UserPreferencesEntity {
        return UserPreferencesEntity(
            userId = userId,
            favoriteActivityTypes = favoriteActivityTypes.joinToString(","),
            preferredWeatherConditions = preferredWeatherConditions.joinToString(","),
            preferredTimeOfDay = preferredTimeOfDay.joinToString(","),
            notificationsEnabled = notificationsEnabled,
            syncEnabled = syncEnabled,
            lastSyncTimestamp = lastSyncTimestamp,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}


fun UserPreferencesEntity.toPreferences(): UserPreferences {
    return UserPreferences(
        userId = userId,
        favoriteActivityTypes = if (favoriteActivityTypes.isNotBlank())
            favoriteActivityTypes.split(",") else emptyList(),
        preferredWeatherConditions = if (preferredWeatherConditions.isNotBlank())
            preferredWeatherConditions.split(",") else emptyList(),
        preferredTimeOfDay = if (preferredTimeOfDay.isNotBlank())
            preferredTimeOfDay.split(",") else emptyList(),
        notificationsEnabled = notificationsEnabled,
        syncEnabled = syncEnabled,
        lastSyncTimestamp = lastSyncTimestamp,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
