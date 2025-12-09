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

    private val _activities = MutableStateFlow<List<ActivityEntity>>(emptyList())
    val activities = _activities.asStateFlow()

    private val _todayActivities = MutableStateFlow<List<ActivityEntity>>(emptyList())
    val todayActivities = _todayActivities.asStateFlow()

    private val _pendingActivities = MutableStateFlow<List<ActivityEntity>>(emptyList())
    val pendingActivities = _pendingActivities.asStateFlow()

    private val _completedActivities = MutableStateFlow<List<ActivityEntity>>(emptyList())
    val completedActivities = _completedActivities.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        loadAllActivities()
        loadTodayActivities()
    }

    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

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

    suspend fun getActivitiesByDate(date: String): List<ActivityEntity> {
        return try {
            repository.getActivitiesByDate(date)
        } catch (e: Exception) {
            _errorMessage.value = "Error loading activities for date: ${e.message}"
            emptyList()
        }
    }

    // UPDATED: Now accepts weatherIconCode parameter
    fun addActivity(
        title: String,
        description: String,
        date: String,
        time: String,
        weatherCondition: String,
        weatherIconCode: String,
        locationType: String,
        category: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val activity = ActivityEntity(
                    title = title,
                    description = description,
                    date = date,
                    time = time,
                    weatherCondition = weatherCondition,
                    weatherIconCode = weatherIconCode, // STORE ICON CODE
                    locationType = locationType,
                    category = category,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis()
                )

                repository.insertActivity(activity)

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

    fun clearError() {
        _errorMessage.value = null
    }
}