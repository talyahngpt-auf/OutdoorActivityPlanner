package ph.edu.auf.thalia.hingpit.outdooractivityplanner.sync


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.UserPreferences


class FirebaseSyncManager {


    private val firestore = FirebaseFirestore.getInstance()


    companion object {
        private const val USERS_COLLECTION = "users"
        private const val ACTIVITIES_COLLECTION = "activities"
        private const val PREFERENCES_COLLECTION = "preferences"
    }


    // ========== ACTIVITIES SYNC ==========


    suspend fun syncActivitiesToCloud(userId: String, activities: List<ActivityEntity>): Result<Unit> {
        return try {
            val batch = firestore.batch()


            activities.forEach { activity ->
                val docRef = firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(ACTIVITIES_COLLECTION)
                    .document(activity.id.toString())


                val activityMap = mapOf(
                    "id" to activity.id,
                    "title" to activity.title,
                    "description" to activity.description,
                    "date" to activity.date,
                    "time" to activity.time,
                    "weatherCondition" to activity.weatherCondition,
                    "weatherIconCode" to activity.weatherIconCode,
                    "isCompleted" to activity.isCompleted,
                    "locationType" to activity.locationType,
                    "category" to activity.category,
                    "createdAt" to activity.createdAt,
                    "updatedAt" to System.currentTimeMillis()
                )


                batch.set(docRef, activityMap, SetOptions.merge())
            }


            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun syncActivitiesFromCloud(userId: String): Result<List<ActivityEntity>> {
        return try {
            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .get()
                .await()


            val activities = snapshot.documents.mapNotNull { doc ->
                try {
                    ActivityEntity(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        weatherCondition = doc.getString("weatherCondition") ?: "",
                        weatherIconCode = doc.getString("weatherIconCode") ?: "01d",
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        locationType = doc.getString("locationType") ?: "",
                        category = doc.getString("category") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }


            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun deleteActivityFromCloud(userId: String, activityId: Int): Result<Unit> {
        return try {
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .document(activityId.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ========== PREFERENCES SYNC ==========


    suspend fun syncPreferencesToCloud(userId: String, preferences: UserPreferences): Result<Unit> {
        return try {
            val preferencesMap = mapOf(
                "userId" to preferences.userId,
                "favoriteActivityTypes" to preferences.favoriteActivityTypes,
                "preferredWeatherConditions" to preferences.preferredWeatherConditions,
                "preferredTimeOfDay" to preferences.preferredTimeOfDay,
                "notificationsEnabled" to preferences.notificationsEnabled,
                "syncEnabled" to preferences.syncEnabled,
                "lastSyncTimestamp" to System.currentTimeMillis(),
                "createdAt" to preferences.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )


            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(PREFERENCES_COLLECTION)
                .document("user_preferences")
                .set(preferencesMap, SetOptions.merge())
                .await()


            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun syncPreferencesFromCloud(userId: String): Result<UserPreferences?> {
        return try {
            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(PREFERENCES_COLLECTION)
                .document("user_preferences")
                .get()
                .await()


            if (snapshot.exists()) {
                val preferences = UserPreferences(
                    userId = snapshot.getString("userId") ?: userId,
                    favoriteActivityTypes = snapshot.get("favoriteActivityTypes") as? List<String> ?: emptyList(),
                    preferredWeatherConditions = snapshot.get("preferredWeatherConditions") as? List<String> ?: emptyList(),
                    preferredTimeOfDay = snapshot.get("preferredTimeOfDay") as? List<String> ?: emptyList(),
                    notificationsEnabled = snapshot.getBoolean("notificationsEnabled") ?: true,
                    syncEnabled = snapshot.getBoolean("syncEnabled") ?: true,
                    lastSyncTimestamp = snapshot.getLong("lastSyncTimestamp") ?: 0L,
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                )
                Result.success(preferences)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ========== FULL SYNC ==========


    suspend fun performFullSync(
        userId: String,
        localActivities: List<ActivityEntity>,
        localPreferences: UserPreferences?
    ): Result<SyncResult> {
        return try {
            // Sync activities to cloud
            syncActivitiesToCloud(userId, localActivities)


            // Sync preferences to cloud
            localPreferences?.let {
                syncPreferencesToCloud(userId, it)
            }


            // Fetch from cloud
            val cloudActivities = syncActivitiesFromCloud(userId).getOrNull() ?: emptyList()
            val cloudPreferences = syncPreferencesFromCloud(userId).getOrNull()


            Result.success(
                SyncResult(
                    activitiesSynced = cloudActivities.size,
                    preferencesSynced = cloudPreferences != null,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ========== DELETE USER DATA ==========


    suspend fun deleteAllUserData(userId: String): Result<Unit> {
        return try {
            // Delete activities
            val activitiesSnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .get()
                .await()


            val batch = firestore.batch()
            activitiesSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }


            // Delete preferences
            val preferencesRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(PREFERENCES_COLLECTION)
                .document("user_preferences")
            batch.delete(preferencesRef)


            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


data class SyncResult(
    val activitiesSynced: Int,
    val preferencesSynced: Boolean,
    val timestamp: Long
)


