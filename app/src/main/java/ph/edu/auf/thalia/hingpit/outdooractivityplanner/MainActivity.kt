package ph.edu.auf.thalia.hingpit.outdooractivityplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.factory.RetrofitFactory
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.interfaces.WeatherApiService
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.AppDatabase
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.ActivityRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.WeatherRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.providers.LocationProvider
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.ActivityPlannerScreen
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.ForecastScreen
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.HomeScreen
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.SettingsScreen
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.theme.OutdoorActivityPlannerTheme
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.Constants
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.ActivityViewModel
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var activityRepository: ActivityRepository
    private lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Room Database
        database = AppDatabase.getDatabase(this)

        // Initialize API
        val weatherApi = RetrofitFactory.create(Constants.WEATHER_BASE_URL)
            .create(WeatherApiService::class.java)

        // Initialize Repositories
        weatherRepository = WeatherRepository(weatherApi, database.weatherCacheDao())
        activityRepository = ActivityRepository(database.activityDao())

        // Initialize LocationProvider
        locationProvider = LocationProvider(this)

        setContent {
            OutdoorActivityPlannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(
                        weatherRepository = weatherRepository,
                        activityRepository = activityRepository,
                        locationProvider = locationProvider
                    )
                }
            }
        }
    }
}

@Composable
fun MainApp(
    weatherRepository: WeatherRepository,
    activityRepository: ActivityRepository,
    locationProvider: LocationProvider
) {
    val navController = rememberNavController()

    // Create ViewModels
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(weatherRepository, locationProvider)
    )

    val activityViewModel: ActivityViewModel = viewModel(
        factory = ActivityViewModelFactory(activityRepository)
    )

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                navController = navController,
                weatherViewModel = weatherViewModel,
                activityViewModel = activityViewModel
            )
        }

        composable("activities") {
            ActivityPlannerScreen(navController)
        }

        composable("forecast") {
            ForecastScreen(
                navController = navController,
                weatherViewModel = weatherViewModel,
                activityViewModel = activityViewModel
            )
        }

        composable("settings") {
            SettingsScreen(navController)
        }
    }
}

// ViewModel Factory for WeatherViewModel
class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(repository, locationProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ViewModel Factory for ActivityViewModel
class ActivityViewModelFactory(
    private val repository: ActivityRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
            return ActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}