package ph.edu.auf.thalia.hingpit.outdooractivityplanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.factory.RetrofitFactory
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.interfaces.WeatherApiService
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.auth.FirebaseAuthManager
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.AppDatabase
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.ActivityRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.UserPreferencesRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.WeatherRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.providers.LocationProvider
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.sync.FirebaseSyncManager
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.*
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.theme.OutdoorActivityPlannerTheme
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.Constants
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.*

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var activityRepository: ActivityRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var locationProvider: LocationProvider
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var syncManager: FirebaseSyncManager

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
        userPreferencesRepository = UserPreferencesRepository(database.userPreferencesDao())

        // Initialize LocationProvider
        locationProvider = LocationProvider(this)

        // Initialize Firebase managers
        authManager = FirebaseAuthManager(this)
        syncManager = FirebaseSyncManager()

        // Handle email link sign-in
        handleEmailLinkSignIn(intent)

        setContent {
            OutdoorActivityPlannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(
                        weatherRepository = weatherRepository,
                        activityRepository = activityRepository,
                        userPreferencesRepository = userPreferencesRepository,
                        locationProvider = locationProvider,
                        authManager = authManager,
                        syncManager = syncManager
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleEmailLinkSignIn(intent)
    }

    private fun handleEmailLinkSignIn(intent: Intent?) {
        val emailLink = intent?.data?.toString()
        if (emailLink != null && authManager.isSignInLink(emailLink)) {
            // Get the email from SharedPreferences or ask user
            val email = authManager.getSavedEmail()
            if (email != null) {
                // Complete sign-in in background
                // This will be handled by AuthViewModel
            }
        }
    }
}

@Composable
fun MainApp(
    weatherRepository: WeatherRepository,
    activityRepository: ActivityRepository,
    userPreferencesRepository: UserPreferencesRepository,
    locationProvider: LocationProvider,
    authManager: FirebaseAuthManager,
    syncManager: FirebaseSyncManager
) {
    val navController = rememberNavController()

    // Create ViewModels
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(weatherRepository, locationProvider)
    )

    val activityViewModel: ActivityViewModel = viewModel(
        factory = ActivityViewModelFactory(activityRepository)
    )

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authManager, syncManager, userPreferencesRepository)
    )

    val syncViewModel: SyncViewModel = viewModel(
        factory = SyncViewModelFactory(authManager, syncManager, activityRepository, userPreferencesRepository)
    )

    // Observe auth state to navigate
    val authState by authViewModel.authState.collectAsState()
    val startDestination = when (authState) {
        is AuthState.Authenticated -> "home"
        is AuthState.Unauthenticated -> "login"
        else -> "login"
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                if (navController.currentDestination?.route == "login" ||
                    navController.currentDestination?.route == "signup") {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                if (navController.currentDestination?.route != "login" &&
                    navController.currentDestination?.route != "signup" &&
                    navController.currentDestination?.route != "forgot_password") {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable("login") {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("signup") {
            SignUpScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("email_link_signin") {
            EmailLinkSignInScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Main app screens
        composable("home") {
            HomeScreen(
                navController = navController,
                weatherViewModel = weatherViewModel,
                activityViewModel = activityViewModel
            )
        }

        composable("today") {
            TodayScreen(
                navController = navController,
                activityViewModel = activityViewModel,
                weatherViewModel = weatherViewModel
            )
        }

        composable("activities") {
            ActivityLogScreen(
                navController = navController,
                activityViewModel = activityViewModel
            )
        }

        composable("forecast") {
            ForecastScreen(
                navController = navController,
                weatherViewModel = weatherViewModel,
                activityViewModel = activityViewModel
            )
        }

        composable("settings") {
            SettingsScreen(
                navController = navController,
                authViewModel = authViewModel,
                syncViewModel = syncViewModel
            )
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

// ViewModel Factory for AuthViewModel
class AuthViewModelFactory(
    private val authManager: FirebaseAuthManager,
    private val syncManager: FirebaseSyncManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authManager, syncManager, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ViewModel Factory for SyncViewModel
class SyncViewModelFactory(
    private val authManager: FirebaseAuthManager,
    private val syncManager: FirebaseSyncManager,
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            return SyncViewModel(authManager, syncManager, activityRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}