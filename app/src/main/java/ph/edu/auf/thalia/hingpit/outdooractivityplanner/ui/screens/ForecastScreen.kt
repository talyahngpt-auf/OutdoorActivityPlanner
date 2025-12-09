package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.getDailyForecasts
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.BottomNavigationBar
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.ExpandedForecastCard
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.AddActivityDialog
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.ScreenHeader
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivitySuggestion
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.ActivityViewModel
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.WeatherViewModel

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

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ActivitySuggestion?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = "5-Day Forecast",
                subtitle = "Weather predictions",
                icon = Icons.Default.List,
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
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

    if (showAddDialog && selectedActivity != null && selectedDate != null) {
        AddActivityDialog(
            activity = selectedActivity,
            weatherData = currentWeather,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, date, time, icon, weatherIconCode, locationType, category ->
                currentWeather?.let { weather ->
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
}