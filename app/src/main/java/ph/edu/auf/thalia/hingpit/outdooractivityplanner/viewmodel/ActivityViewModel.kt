package ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.ActivityRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityViewModel(
    private val repository: ActivityRepository
) : ViewModel() {

    // All activities
    private val _activities = MutableStateFlow<List<ActivityEntity>>(emptyList())
    val activities = _activities.asStateFlow()

    // Today's activities
    private val _todayActivities = MutableStateFlow<List<ActivityEntity>>(emptyList())
    val todayActivities = _todayActivities.asStateFlow()

    // Pending activities
    private val _pendingActivities = MutableStateFlow<List<ActivityEntity>>(emptyList())
    val pendingActivities = _pendingActivities.asStateFlow()

    // Completed activities
    private val _completedActivities = MutableStateFlow<List<ActivityEntity>>(emptyList())
    val completedActivities = _completedActivities.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        loadAllActivities()
        loadTodayActivities()
    }

    // Get today's date in yyyy-MM-dd format
    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Load all activities
    fun loadAllActivities() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val results = repository.getAllActivities()
                _activities.value = results
            } catch (e: Exception) {
                _errorMessage.value = "Error loading activities: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load today's activities
    fun loadTodayActivities() {
        viewModelScope.launch {
            try {
                val today = getTodayDate()
                val results = repository.getActivitiesByDate(today)
                _todayActivities.value = results
            } catch (e: Exception) {
                _errorMessage.value = "Error loading today's activities: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    // Load pending activities
    fun loadPendingActivities() {
        viewModelScope.launch {
            try {
                val results = repository.getPendingActivities()
                _pendingActivities.value = results
            } catch (e: Exception) {
                _errorMessage.value = "Error loading pending activities: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    // Load completed activities
    fun loadCompletedActivities() {
        viewModelScope.launch {
            try {
                val results = repository.getCompletedActivities()
                _completedActivities.value = results
            } catch (e: Exception) {
                _errorMessage.value = "Error loading completed activities: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    // Get activities by date
    suspend fun getActivitiesByDate(date: String): List<ActivityEntity> {
        return try {
            repository.getActivitiesByDate(date)
        } catch (e: Exception) {
            _errorMessage.value = "Error loading activities for date: ${e.message}"
            emptyList()
        }
    }

    // Add new activity
    fun addActivity(
        title: String,
        description: String,
        date: String,
        weatherCondition: String,
        time: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val activity = ActivityEntity(
                    title = title,
                    description = description,
                    date = date,
                    weatherCondition = weatherCondition,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis()
                )

                repository.insertActivity(activity)

                // Reload activities
                loadAllActivities()
                loadTodayActivities()
                loadPendingActivities()

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error adding activity: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update activity
    fun updateActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateActivity(activity)

                loadAllActivities()
                loadTodayActivities()
                loadPendingActivities()
                loadCompletedActivities()

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error updating activity: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Toggle completion status
    fun toggleActivityCompletion(activity: ActivityEntity) {
        viewModelScope.launch {
            try {
                val updatedActivity = activity.copy(isCompleted = !activity.isCompleted)
                repository.updateActivity(updatedActivity)

                loadAllActivities()
                loadTodayActivities()
                loadPendingActivities()
                loadCompletedActivities()
            } catch (e: Exception) {
                _errorMessage.value = "Error toggling activity: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    // Delete activity
    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteActivity(activity)

                loadAllActivities()
                loadTodayActivities()
                loadPendingActivities()
                loadCompletedActivities()

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting activity: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }
}