package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens




import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.DailyForecastSummary
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.getDailyForecasts
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.ActivityCard
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.WeatherCard
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivitySuggestion
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivitySuggestions
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.Constants
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.QuickEmojiSelector
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.ActivityViewModel
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*




@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel,
    activityViewModel: ActivityViewModel
) {
    val scope = rememberCoroutineScope()




    // Weather state
    val weatherData by weatherViewModel.currentWeather.collectAsState()
    val forecast by weatherViewModel.forecast.collectAsState()
    val isLoadingWeather by weatherViewModel.isLoading.collectAsState()
    val weatherError by weatherViewModel.errorMessage.collectAsState()




    // Activity suggestions state
    var activitySuggestions by remember { mutableStateOf<List<ActivitySuggestion>>(emptyList()) }
    var refreshKey by remember { mutableStateOf(0) }




    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var showCustomActivityDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ActivitySuggestion?>(null) }




    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }




    // Permission handling
    val locationPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    )




    // Update suggestions when weather changes or refresh
    LaunchedEffect(weatherData, refreshKey) {
        weatherData?.let { weather ->
            val condition = weather.weather.firstOrNull()?.main ?: "Clear"
            val temp = weather.main.temp
            activitySuggestions = ActivitySuggestions.getSuggestions(condition, temp, limit = 5)
        }
    }




    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Outdoor Activity Planner") },
                actions = {
                    IconButton(onClick = { navController.navigate("forecast") }) {
                        Icon(Icons.Default.List, contentDescription = "View Forecast Details")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCustomActivityDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom Activity")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }




            // Permission Request Card
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




            // Action Buttons Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Button (toggles search bar)
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




                    // Current Location Button
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




            // Search Bar (animated, only shows when showSearchBar is true)
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




            // Loading State
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




            // Error State
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




            // Current Weather Card
            weatherData?.let { weather ->
                item {
                    ImprovedWeatherCard(weather = weather)
                }




                // 5-Day Forecast
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




                // Today's Suggested Activities
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




            // Empty state
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




            item { Spacer(modifier = Modifier.height(80.dp)) } // Extra space for FAB
        }
    }




    // Add to Plan Dialog (from suggestions)
    if (showAddDialog && selectedActivity != null) {
        AddActivityDialog(
            activity = selectedActivity,
            weatherData = weatherData,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, date, time, icon ->
                weatherData?.let { weather ->
                    activityViewModel.addActivity(
                        title = title,
                        description = description,
                        date = date,
                        time = time,
                        weatherCondition = weather.weather.firstOrNull()?.main ?: "Clear"
                    )
                }
                showAddDialog = false
            }
        )
    }




    // Custom Activity Dialog (FAB)
    if (showCustomActivityDialog) {
        AddActivityDialog(
            activity = null, // No pre-filled activity
            weatherData = weatherData,
            onDismiss = { showCustomActivityDialog = false },
            onConfirm = { title, description, date, time, icon ->
                weatherData?.let { weather ->
                    activityViewModel.addActivity(
                        title = title,
                        description = description,
                        date = date,
                        time = time,
                        weatherCondition = weather.weather.firstOrNull()?.main ?: "Clear"
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
            // Header with city and icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "https://openweathermap.org/img/wn/${weather.weather.firstOrNull()?.icon}@2x.png",
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
                        text = "${weather.main.temp.toInt()}°C",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weather.weather.firstOrNull()?.description
                            ?.replaceFirstChar { it.uppercase() } ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }




            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))




            // Weather details in a grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(
                    label = "Humidity",
                    value = "💧 ${weather.main.humidity}%",
                    icon = ""
                )
                WeatherDetailItem(
                    label = "Wind Speed",
                    value = "💨 ${weather.wind.speed} m/s",
                    icon = ""
                )
                WeatherDetailItem(
                    label = "Feels Like",
                    value = "🌡️ ${weather.main.feelsLike.toInt()}°C",
                    icon = ""
                )
            }
        }
    }
}




@Composable
fun WeatherDetailItem(
    label: String,
    value: String,
    icon: String
) {
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




@Composable
fun SimpleForecastCard(dailySummary: DailyForecastSummary) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = dateFormat.parse(dailySummary.date) ?: Date()
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dayOfWeek = dayFormat.format(date)




    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )




            Spacer(modifier = Modifier.height(8.dp))




            AsyncImage(
                model = "https://openweathermap.org/img/wn/${dailySummary.weather.firstOrNull()?.icon}@2x.png",
                contentDescription = "Weather Icon",
                modifier = Modifier.size(40.dp)
            )




            Spacer(modifier = Modifier.height(8.dp))




            Text(
                text = "${dailySummary.maxTemp.toInt()}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${dailySummary.minTemp.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}




// Universal Activity Dialog (works for both suggestions and custom activities)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityDialog(
    activity: ActivitySuggestion?,
    weatherData: ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.CurrentWeatherResponse?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, date: String, time: String, icon: String) -> Unit
) {
    var title by remember { mutableStateOf(activity?.title ?: "") }
    var description by remember { mutableStateOf(activity?.description ?: "") }
    var selectedIcon by remember { mutableStateOf(activity?.icon ?: "📌") }


    // Date & Time states
    val calendar = remember { Calendar.getInstance() }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())


    var selectedDate by remember { mutableStateOf(dateFormat.format(calendar.time)) }
    var selectedTime by remember {
        mutableStateOf(
            activity?.timeOfDay?.let {
                when (it) {
                    "morning" -> "08:00"
                    "afternoon" -> "14:00"
                    "evening" -> "18:00"
                    "night" -> "20:00"
                    else -> timeFormat.format(calendar.time)
                }
            } ?: timeFormat.format(calendar.time)
        )
    }


    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.split(":")[0].toInt(),
        initialMinute = selectedTime.split(":")[1].toInt()
    )


    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (activity != null) "Add to Plan" else "Create Activity",
                    modifier = Modifier.weight(1f)
                )
            }
        },
        text = {
            LazyColumn {
                item {
                    // Weather info
                    weatherData?.let { weather ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Weather: ${weather.weather.firstOrNull()?.main ?: ""}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${weather.main.temp.toInt()}°C",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }


                item {
                    // Quick Emoji Selector with full keyboard access
                    QuickEmojiSelector(
                        selectedEmoji = selectedIcon,
                        onEmojiChange = { selectedIcon = it },
                        modifier = Modifier.fillMaxWidth()
                    )


                    Spacer(modifier = Modifier.height(16.dp))
                }


                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Activity Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Text(
                                text = selectedIcon,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    )


                    Spacer(modifier = Modifier.height(12.dp))
                }


                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )


                    Spacer(modifier = Modifier.height(12.dp))
                }


                item {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Date: $selectedDate")
                    }


                    Spacer(modifier = Modifier.height(8.dp))
                }


                item {
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Time: $selectedTime")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), description.trim(), selectedDate, selectedTime, selectedIcon)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )


    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = dateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = String.format(
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}




// Bottom Navigation Bar Component
@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route




    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )




        NavigationBarItem(
            selected = currentRoute == "today",
            onClick = {
                if (currentRoute != "today") {
                    navController.navigate("today")
                }
            },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Today") },
            label = { Text("Today") }
        )




        NavigationBarItem(
            selected = currentRoute == "activities",
            onClick = {
                if (currentRoute != "activities") {
                    navController.navigate("activities")
                }
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Activity Log") },
            label = { Text("Log") }
        )




        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = {
                if (currentRoute != "settings") {
                    navController.navigate("settings")
                }
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}

