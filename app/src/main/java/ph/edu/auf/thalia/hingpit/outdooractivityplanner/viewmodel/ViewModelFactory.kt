package ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.auth.FirebaseAuthManager
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.ActivityRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.UserPreferencesRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.WeatherRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.providers.LocationProvider
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.sync.FirebaseSyncManager


class AppViewModelFactory(
    private val weatherRepository: WeatherRepository? = null,
    private val activityRepository: ActivityRepository? = null,
    private val locationProvider: LocationProvider? = null,
    private val authManager: FirebaseAuthManager? = null,
    private val syncManager: FirebaseSyncManager? = null,
    private val userPreferencesRepository: UserPreferencesRepository? = null
) : ViewModelProvider.Factory {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(WeatherViewModel::class.java) -> {
                require(weatherRepository != null && locationProvider != null) {
                    "WeatherRepository and LocationProvider required for WeatherViewModel"
                }
                WeatherViewModel(weatherRepository, locationProvider) as T
            }


            modelClass.isAssignableFrom(ActivityViewModel::class.java) -> {
                require(activityRepository != null) {
                    "ActivityRepository required for ActivityViewModel"
                }
                ActivityViewModel(activityRepository) as T
            }


            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                require(authManager != null && syncManager != null && userPreferencesRepository != null) {
                    "AuthManager, SyncManager, and UserPreferencesRepository required for AuthViewModel"
                }
                AuthViewModel(authManager, syncManager, userPreferencesRepository) as T
            }


            modelClass.isAssignableFrom(SyncViewModel::class.java) -> {
                require(
                    authManager != null &&
                            syncManager != null &&
                            activityRepository != null &&
                            userPreferencesRepository != null
                ) {
                    "All repositories and managers required for SyncViewModel"
                }
                SyncViewModel(authManager, syncManager, activityRepository, userPreferencesRepository) as T
            }


            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
