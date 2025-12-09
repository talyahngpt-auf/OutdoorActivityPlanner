package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.getDailyForecasts
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.*
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivityMasterList
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivitySuggestion
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.Constants
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.TimeOfDay
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.WeatherCondition
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.WeatherUtils
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.ActivityViewModel
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.WeatherViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel,
    activityViewModel: ActivityViewModel
) {
    val scope = rememberCoroutineScope()

    val weatherData by weatherViewModel.currentWeather.collectAsState()
    val forecast by weatherViewModel.forecast.collectAsState()
    val isLoadingWeather by weatherViewModel.isLoading.collectAsState()
    val weatherError by weatherViewModel.errorMessage.collectAsState()

    var activitySuggestions by remember { mutableStateOf<List<ActivitySuggestion>>(emptyList()) }
    var refreshKey by remember { mutableStateOf(0) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showCustomActivityDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ActivitySuggestion?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(weatherData, refreshKey) {
        weatherData?.let { weather ->
            val weatherCondition = WeatherCondition.fromString(
                weather.weather.firstOrNull()?.main ?: "Clear"
            )
            val currentTimeOfDay = TimeOfDay.getCurrentTimeOfDay()
            val currentTemp = weather.main.temp

            activitySuggestions = ActivityMasterList.getSuggestions(
                weatherCondition = weatherCondition,
                temperature = currentTemp,
                currentTime = currentTimeOfDay,
                limit = 5
            ).map { activity: ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.Activity ->
                ActivitySuggestion(
                    title = activity.title,
                    description = activity.description,
                    category = activity.category.toDisplayString(),
                    icon = activity.icon,
                    timeOfDay = activity.timesOfDay.firstOrNull()?.name?.lowercase(),
                    location = activity.locationType.toDisplayString()
                )
            }
        }
    }

    Scaffold(
        topBar = {
            HomeHeader(
                actions = {
                    IconButton(onClick = { navController.navigate("forecast") }) {
                        Icon(Icons.Default.List, "Forecast")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        // ✅ ADD THIS: Floating Action Button
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCustomActivityDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Activity")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (!locationPermissionState.status.isGranted) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📍 Location Permission Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (locationPermissionState.status.shouldShowRationale) {
                                    "We need location access to provide weather-based activity suggestions for your area."
                                } else {
                                    "Please grant location permission to get personalized activity suggestions."
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { locationPermissionState.launchPermissionRequest() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showSearchBar = !showSearchBar },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showSearchBar) "Close" else "Search City")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                if (locationPermissionState.status.isGranted) {
                                    weatherViewModel.getCurrentLocation()
                                } else {
                                    locationPermissionState.launchPermissionRequest()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoadingWeather
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("My Location")
                    }
                }
            }

            if (showSearchBar) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("e.g., Angeles,PH") },
                            leadingIcon = { Icon(Icons.Default.Search, "Search") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )

                        Button(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    scope.launch {
                                        weatherViewModel.getCurrentWeatherByCity(
                                            searchQuery.trim(),
                                            Constants.WEATHER_API_KEY
                                        )
                                    }
                                }
                            },
                            enabled = searchQuery.isNotBlank() && !isLoadingWeather
                        ) {
                            Text("Go")
                        }
                    }
                }
            }

            if (isLoadingWeather) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Fetching weather data...")
                            }
                        }
                    }
                }
            }

            weatherError?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⚠️ Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        if (locationPermissionState.status.isGranted) {
                                            weatherViewModel.getCurrentLocation()
                                        }
                                    }
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            weatherData?.let { weather ->
                item {
                    ImprovedWeatherCard(weather = weather)
                }

                forecast?.let { forecastData ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "5-Day Forecast",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { navController.navigate("forecast") }) {
                                Text("View More")
                            }
                        }
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(forecastData.getDailyForecasts()) { dailySummary ->
                                SimpleForecastCard(dailySummary)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Suggested Activities Today",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { refreshKey++ }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Suggestions")
                        }
                    }
                }

                items(activitySuggestions) { suggestion ->
                    ActivityCard(
                        activity = suggestion,
                        onAddToPlan = {
                            selectedActivity = suggestion
                            showAddDialog = true
                        }
                    )
                }
            }

            if (weatherData == null && !isLoadingWeather && weatherError == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🌤️", style = MaterialTheme.typography.displayLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Welcome!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Search for a city or use your current location to get started.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Dialog for adding suggested activities
    if (showAddDialog && selectedActivity != null) {
        AddActivityDialog(
            activity = selectedActivity,
            weatherData = weatherData,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, date, time, icon, weatherIconCode, locationType, category ->
                weatherData?.let { weather ->
                    activityViewModel.addActivity(
                        title = title,
                        description = description,
                        date = date,
                        time = time,
                        weatherCondition = weather.weather.firstOrNull()?.main ?: "Clear",
                        weatherIconCode = weatherIconCode,
                        locationType = locationType,
                        category = category
                    )
                }
                showAddDialog = false
            }
        )
    }

    // ✅ Dialog for creating custom activities from scratch
    if (showCustomActivityDialog) {
        AddActivityDialog(
            activity = null,  // Pass null to create from scratch
            weatherData = weatherData,
            onDismiss = { showCustomActivityDialog = false },
            onConfirm = { title, description, date, time, icon, weatherIconCode, locationType, category ->
                weatherData?.let { weather ->
                    activityViewModel.addActivity(
                        title = title,
                        description = description,
                        date = date,
                        time = time,
                        weatherCondition = weather.weather.firstOrNull()?.main ?: "Clear",
                        weatherIconCode = weatherIconCode,
                        locationType = locationType,
                        category = category
                    )
                }
                showCustomActivityDialog = false
            }
        )
    }
}


@Composable
fun ImprovedWeatherCard(weather: ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.CurrentWeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = WeatherUtils.getWeatherIconUrl(
                        weather.weather.firstOrNull()?.icon ?: "01d"
                    ),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = weather.city,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = WeatherUtils.formatTemperature(weather.main.temp),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = WeatherUtils.formatWeatherDescription(
                            weather.weather.firstOrNull()?.description ?: ""
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(
                    value = "💧 ${WeatherUtils.formatHumidity(weather.main.humidity)}"
                )
                WeatherDetailItem(
                    value = "💨 ${WeatherUtils.formatWindSpeed(weather.wind.speed)}"
                )
                WeatherDetailItem(
                    value = "🌡️ ${WeatherUtils.formatTemperature(weather.main.feelsLike)}"
                )
            }
        }
    }
}


@Composable
private fun WeatherDetailItem(value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}