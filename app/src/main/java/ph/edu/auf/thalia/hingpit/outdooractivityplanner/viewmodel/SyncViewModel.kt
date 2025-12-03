package ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.auth.FirebaseAuthManager
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.ActivityRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.UserPreferencesRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.sync.FirebaseSyncManager
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.sync.SyncResult

class SyncViewModel(
    private val authManager: FirebaseAuthManager,
    private val syncManager: FirebaseSyncManager,
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp = _lastSyncTimestamp.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        loadLastSyncTime()
    }

    private fun loadLastSyncTime() {
        viewModelScope.launch {
            try {
                val userId = authManager.currentUserId
                if (userId != null) {
                    val preferences = userPreferencesRepository.getPreferences(userId)
                    _lastSyncTimestamp.value = preferences?.lastSyncTimestamp
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    fun performSync() {
        viewModelScope.launch {
            try {
                val userId = authManager.currentUserId
                if (userId == null) {
                    _errorMessage.value = "No user signed in"
                    return@launch
                }

                _isSyncing.value = true
                _syncStatus.value = SyncStatus.Syncing
                _errorMessage.value = null

                // Get local data
                val localActivities = activityRepository.getAllActivities()
                val localPreferences = userPreferencesRepository.getPreferences(userId)

                // Perform full sync
                val result = syncManager.performFullSync(
                    userId = userId,
                    localActivities = localActivities,
                    localPreferences = localPreferences
                )

                if (result.isSuccess) {
                    val syncResult = result.getOrNull()
                    if (syncResult != null) {
                        // Update last sync timestamp
                        userPreferencesRepository.updateLastSyncTimestamp(
                            userId,
                            syncResult.timestamp
                        )
                        _lastSyncTimestamp.value = syncResult.timestamp
                        _syncStatus.value = SyncStatus.Success(syncResult)
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Sync failed"
                    _syncStatus.value = SyncStatus.Error(result.exceptionOrNull()?.message ?: "Sync failed")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred during sync"
                _syncStatus.value = SyncStatus.Error(e.message ?: "An error occurred")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun syncActivitiesToCloud() {
        viewModelScope.launch {
            try {
                val userId = authManager.currentUserId
                if (userId == null) {
                    _errorMessage.value = "No user signed in"
                    return@launch
                }

                _isSyncing.value = true
                _errorMessage.value = null

                val activities = activityRepository.getAllActivities()
                val result = syncManager.syncActivitiesToCloud(userId, activities)

                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to sync activities"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun syncActivitiesFromCloud() {
        viewModelScope.launch {
            try {
                val userId = authManager.currentUserId
                if (userId == null) {
                    _errorMessage.value = "No user signed in"
                    return@launch
                }

                _isSyncing.value = true
                _errorMessage.value = null

                val result = syncManager.syncActivitiesFromCloud(userId)

                if (result.isSuccess) {
                    val cloudActivities = result.getOrNull() ?: emptyList()
                    // Insert cloud activities into local database
                    activityRepository.insertActivities(cloudActivities)
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to fetch activities"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun syncPreferencesToCloud() {
        viewModelScope.launch {
            try {
                val userId = authManager.currentUserId
                if (userId == null) {
                    _errorMessage.value = "No user signed in"
                    return@launch
                }

                _isSyncing.value = true
                _errorMessage.value = null

                val preferences = userPreferencesRepository.getPreferences(userId)
                if (preferences != null) {
                    val result = syncManager.syncPreferencesToCloud(userId, preferences)

                    if (result.isFailure) {
                        _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to sync preferences"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus.Idle
    }
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val result: SyncResult) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}