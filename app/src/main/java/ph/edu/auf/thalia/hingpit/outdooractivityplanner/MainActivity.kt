package ph.edu.auf.thalia.hingpit.outdooractivityplanner


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.factory.RetrofitFactory
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.interfaces.WeatherApiService
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.auth.FirebaseAuthManager
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.AppDatabase
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.ActivityRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.WeatherRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.providers.LocationProvider
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.sync.FirebaseSyncManager
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.*
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.theme.OutdoorActivityPlannerTheme
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.Constants
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.*
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.UserPreferencesRepository


class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val weatherApi by lazy {
        RetrofitFactory.create(Constants.WEATHER_BASE_URL)
            .create(WeatherApiService::class.java)
    }


    private val weatherRepository by lazy {
        WeatherRepository(weatherApi, database.weatherCacheDao())
    }
    private val activityRepository by lazy {
        ActivityRepository(database.activityDao())
    }
    private val locationProvider by lazy { LocationProvider(this) }
    private val authManager by lazy { FirebaseAuthManager(this) }
    private val syncManager by lazy { FirebaseSyncManager() }


    private val userPreferencesRepository by lazy { UserPreferencesRepository(database.userPreferencesDao()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            OutdoorActivityPlannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(
                        weatherRepository = weatherRepository,
                        activityRepository = activityRepository,
                        locationProvider = locationProvider,
                        authManager = authManager,
                        syncManager = syncManager,
                        userPreferencesRepository = userPreferencesRepository
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
    locationProvider: LocationProvider,
    authManager: FirebaseAuthManager,
    syncManager: FirebaseSyncManager,
    userPreferencesRepository: UserPreferencesRepository
) {
    val navController = rememberNavController()
    val context = LocalContext.current




    // Request location permissions at startup
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineGranted || coarseGranted) {
                // Permissions granted → LocationProvider can be used safely
            }
        }
    )


    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)


        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val weatherFactory = remember {
        AppViewModelFactory(
            weatherRepository = weatherRepository,
            locationProvider = locationProvider
        )
    }


    val activityFactory = remember {
        AppViewModelFactory(activityRepository = activityRepository)
    }


    val authFactory = remember {
        AppViewModelFactory(
            authManager = authManager,
            syncManager = syncManager,
            userPreferencesRepository = userPreferencesRepository
        )
    }


    val syncFactory = remember {
        AppViewModelFactory(
            authManager = authManager,
            syncManager = syncManager,
            activityRepository = activityRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    }


    val weatherViewModel: WeatherViewModel = viewModel(factory = weatherFactory)
    val activityViewModel: ActivityViewModel = viewModel(factory = activityFactory)
    val authViewModel: AuthViewModel = viewModel(factory = authFactory)
    val syncViewModel: SyncViewModel = viewModel(factory = syncFactory)


    val authState by authViewModel.authState.collectAsState()


    // Track if splash was shown
    var hasShownSplash by remember { mutableStateOf(false) }


    val startDestination = if (!hasShownSplash) {
        "splash"
    } else {
        when (authState) {
            is AuthState.Authenticated -> "home"
            is AuthState.Unauthenticated -> "login"
            else -> "login"
        }
    }

    LaunchedEffect(authState) {
        // Only navigate after splash is done
        if (hasShownSplash) {
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
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                activityViewModel.setCurrentUser(state.user.uid)
            }
            is AuthState.Unauthenticated -> {
                activityViewModel.setCurrentUser("") // Clear activities
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel,
                weatherViewModel = weatherViewModel,
                onSplashComplete = { hasShownSplash = true }
            )
        }


        // Auth screens
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("signup") {
            SignUpScreen(navController, authViewModel)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController, authViewModel)
        }


        // Main app screens
        composable("home") {
            HomeScreen(navController, weatherViewModel, activityViewModel)
        }
        composable("today") {
            TodayScreen(navController, activityViewModel, weatherViewModel)
        }
        composable("activities") {
            ActivityLogScreen(navController, activityViewModel)
        }
        composable("forecast") {
            ForecastScreen(navController, weatherViewModel, activityViewModel)
        }
        composable("settings") {
            SettingsScreen(navController, authViewModel, syncViewModel)
        }
        composable("account_management") {
            AccountManagementScreen(navController, authViewModel)
        }
    }
}
