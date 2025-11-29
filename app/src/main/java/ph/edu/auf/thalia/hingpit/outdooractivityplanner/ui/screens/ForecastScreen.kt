package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.CurrentWeatherResponse
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.DailyForecastSummary
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.getDailyForecasts
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.ActivityCard
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivitySuggestion
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivitySuggestions
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.ActivityViewModel
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    navController: NavHostController,
    weatherViewModel: WeatherViewModel,
    activityViewModel: ActivityViewModel
) {
    val forecast by weatherViewModel.forecast.collectAsState()
    val currentWeather by weatherViewModel.currentWeather.collectAsState()
    val isLoading by weatherViewModel.isLoading.collectAsState()

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ActivitySuggestion?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("5-Day Forecast") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
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

            // Current Location Info
            currentWeather?.let { weather ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📍 ${weather.city}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = "5-Day Weather Forecast & Activity Suggestions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Loading State
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Forecast Cards with Activity Suggestions
            forecast?.let { forecastData ->
                items(forecastData.getDailyForecasts()) { dailyForecast ->
                    ExpandedForecastCard(
                        dailyForecast = dailyForecast,
                        onActivityClick = { activity, date ->
                            selectedActivity = activity
                            selectedDate = date
                            showAddDialog = true
                        }
                    )
                }
            }

            // Empty State
            if (forecast == null && !isLoading) {
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
                            Text(
                                text = "📅",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Forecast Available",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Please check weather from the home screen first",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Add to Plan Dialog
    if (showAddDialog && selectedActivity != null && selectedDate != null) {
        AddToPlanDialog(
            activity = selectedActivity!!,
            weatherData = currentWeather,
            forecastDate = selectedDate!!,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, date, time ->
                currentWeather?.let { weather ->
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
}

@Composable
fun ExpandedForecastCard(
    dailyForecast: DailyForecastSummary,
    onActivityClick: (ActivitySuggestion, String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dailyForecast.date) ?: Date()
    val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val dayOfWeek = dayFormat.format(date)
    val dateString = dateFormat.format(date)

    // Get activity suggestions for this day
    val condition = dailyForecast.weather.firstOrNull()?.main ?: "Clear"
    val suggestions = ActivitySuggestions.getSuggestions(condition, dailyForecast.avgTemp, limit = 4)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Weather Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "https://openweathermap.org/img/wn/${dailyForecast.weather.firstOrNull()?.icon}@2x.png",
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dayOfWeek,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dailyForecast.weather.firstOrNull()?.description
                            ?.replaceFirstChar { it.uppercase() } ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${dailyForecast.maxTemp.toInt()}°",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${dailyForecast.minTemp.toInt()}°",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
            }

            // Weather Details
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ForecastWeatherDetailItem(
                    label = "Humidity",
                    value = "${dailyForecast.humidity}%",
                    icon = "💧"
                )
                ForecastWeatherDetailItem(
                    label = "Wind",
                    value = "${dailyForecast.windSpeed.toInt()} m/s",
                    icon = "💨"
                )
                ForecastWeatherDetailItem(
                    label = "Avg Temp",
                    value = "${dailyForecast.avgTemp.toInt()}°C",
                    icon = "🌡️"
                )
            }

            // Activity Suggestions (Expandable)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suggested Activities",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Collapse",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                suggestions.forEach { suggestion ->
                    ActivityCard(
                        activity = suggestion,
                        onAddToPlan = { onActivityClick(suggestion, dailyForecast.date) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suggested Activities",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastWeatherDetailItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Bottom Navigation Bar Component
@Composable
fun BottomNavigationBar(navController: NavHostController) {
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

// Add this new dialog composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlanDialog(
    activity: ActivitySuggestion,
    weatherData: CurrentWeatherResponse?,
    forecastDate: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, date: String, time: String) -> Unit
) {
    var title by remember { mutableStateOf(activity.title) }
    var description by remember { mutableStateOf(activity.description) }
    var selectedDate by remember { mutableStateOf(forecastDate) }
    var selectedTime by remember {
        mutableStateOf(
            when (activity.timeOfDay) {
                "morning" -> "08:00"
                "afternoon" -> "14:00"
                "evening" -> "18:00"
                "night" -> "20:00"
                else -> "12:00"
            }
        )
    }

    // Date & Time Picker states
    val calendar = remember { Calendar.getInstance() }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Parse the forecast date to get initial date picker state
    val initialDate = try {
        dateFormat.parse(forecastDate)?.time ?: calendar.timeInMillis
    } catch (e: Exception) {
        calendar.timeInMillis
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
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
                Text(text = activity.icon, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add to Plan")
            }
        },
        text = {
            Column {
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Date: $selectedDate")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Picker Button
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Time: $selectedTime")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), description.trim(), selectedDate, selectedTime)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add to Plan")
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