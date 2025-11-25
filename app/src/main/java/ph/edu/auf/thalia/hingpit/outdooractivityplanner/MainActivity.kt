package ph.edu.auf.thalia.hingpit.outdooractivityplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.launch
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.factory.RetrofitFactory
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.interfaces.WeatherApiService
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.WeatherCache
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.WeatherRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.providers.LocationProvider
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.theme.OutdoorActivityPlannerTheme
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.Constants
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    private lateinit var realm: Realm
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Realm
        val config = RealmConfiguration.Builder(
            schema = setOf(ActivityEntity::class, WeatherCache::class)
        ).name("test.realm").build()
        realm = Realm.open(config)

        // Initialize API
        val weatherApi = RetrofitFactory.create(Constants.WEATHER_BASE_URL)
            .create(WeatherApiService::class.java)
        weatherRepository = WeatherRepository(weatherApi, realm)

        // Initialize LocationProvider
        locationProvider = LocationProvider(this)

        setContent {
            OutdoorActivityPlannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create ViewModel with factory
                    val viewModel: WeatherViewModel = viewModel(
                        factory = WeatherViewModelFactory(weatherRepository, locationProvider)
                    )

                    WeatherTestScreen(viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}

// ViewModel Factory to pass dependencies
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherTestScreen(viewModel: WeatherViewModel) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Permission handling
    val locationPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    // State variables
    var cityInput by remember { mutableStateOf("") }
    val weatherData by viewModel.currentWeather.collectAsState()
    val forecast by viewModel.forecast.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Weather API Testing",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Permission Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (locationPermissionState.status.isGranted)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (locationPermissionState.status.isGranted)
                        "✅ Location Permission Granted"
                    else
                        "❌ Location Permission Required",
                    style = MaterialTheme.typography.titleMedium
                )

                if (!locationPermissionState.status.isGranted) {
                    Spacer(modifier = Modifier.height(8.dp))

                    val rationale = if (locationPermissionState.status.shouldShowRationale) {
                        "Location is needed to fetch weather for your current area"
                    } else {
                        "Location permission is required for this feature"
                    }

                    Text(text = rationale, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { locationPermissionState.launchPermissionRequest() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // City Search Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Test by City Name",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Info card about city naming
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = "💡 Tip: Try without 'City' suffix first (e.g., 'Bacolor' instead of 'Bacolor City'). The app will auto-correct if needed.",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = { Text("Enter City Name") },
                    placeholder = { Text("e.g., Angeles, Manila, Tokyo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (cityInput.isNotBlank()) {
                            scope.launch {
                                viewModel.getCurrentWeatherByCity(
                                    cityInput.trim(),
                                    Constants.WEATHER_API_KEY
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = cityInput.isNotBlank() && !isLoading
                ) {
                    Text(if (isLoading) "Loading..." else "Fetch Weather")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Test Buttons
        Text(
            text = "Quick Test Cities",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.getCurrentWeatherByCity("Angeles", Constants.WEATHER_API_KEY)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Angeles")
            }

            Button(
                onClick = {
                    scope.launch {
                        viewModel.getCurrentWeatherByCity("Manila", Constants.WEATHER_API_KEY)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Manila")
            }

            Button(
                onClick = {
                    scope.launch {
                        viewModel.getCurrentWeatherByCity("Tokyo", Constants.WEATHER_API_KEY)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Tokyo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location-based Test (requires permission)
        if (locationPermissionState.status.isGranted) {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.getCurrentLocation()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Get Weather at My Location")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Loading Indicator
        if (isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Loading weather data...")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Error Message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "⚠️ $error",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Weather Results Display
        weatherData?.let { weather ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✅ Weather Data Retrieved",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📍 Location: ${weather.city}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "🌡️ Temperature: ${weather.main.temp}°C",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "🌡️ Feels Like: ${weather.main.feelsLike}°C",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "💧 Humidity: ${weather.main.humidity}%",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "💨 Wind Speed: ${weather.wind.speed} m/s",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    weather.weather.firstOrNull()?.let { condition ->
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "☁️ Condition: ${condition.main}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "📝 Description: ${condition.description}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "🔖 Icon Code: ${condition.icon}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🌍 Coordinates:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Lat: ${weather.coord.lat}, Lon: ${weather.coord.lon}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Forecast Data (if available)
        forecast?.let { forecastData ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📅 7-Day Forecast",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    forecastData.daily.take(7).forEachIndexed { index, daily ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "Day ${index + 1}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "🌡️ ${daily.temperature.min}°C - ${daily.temperature.max}°C (Day: ${daily.temperature.day}°C)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "☁️ ${daily.weather.firstOrNull()?.main ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            if (index < forecastData.daily.size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}