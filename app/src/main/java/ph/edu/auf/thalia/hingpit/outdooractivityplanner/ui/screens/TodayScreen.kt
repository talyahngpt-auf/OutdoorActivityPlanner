package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.delay
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.*
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivityCategory
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.DateUtils
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.LocationType
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.TimeOfDayUtils
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.ActivityViewModel
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodayScreen(
    navController: NavHostController,
    activityViewModel: ActivityViewModel,
    weatherViewModel: WeatherViewModel
) {
    val todayActivities by activityViewModel.todayActivities.collectAsState()
    val currentWeather by weatherViewModel.currentWeather.collectAsState()
    val isLoading by activityViewModel.isLoading.collectAsState()
    val errorMessage by activityViewModel.errorMessage.collectAsState()

    // Filters
    var selectedCategory by remember { mutableStateOf<ActivityCategory?>(null) }
    var selectedLocation by remember { mutableStateOf<LocationType?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ActivityEntity?>(null) }

    var showCelebration by remember { mutableStateOf(false) }

    val completedCount = todayActivities.count { it.isCompleted }
    val pendingCount = todayActivities.count { !it.isCompleted }
    val totalCount = todayActivities.size
    val progressPercentage = if (totalCount > 0) (completedCount.toFloat() / totalCount) * 100 else 0f

    // Apply category/location filters and sort: pending first, completed last
    val filteredTodayActivities = remember(todayActivities, selectedCategory, selectedLocation) {
        todayActivities.filter { activity ->
            val categoryMatch = selectedCategory?.let { activity.category == it.toDisplayString() } ?: true
            val locationMatch = selectedLocation?.let { activity.locationType == it.toDisplayString() } ?: true
            categoryMatch && locationMatch
        }.sortedBy { it.isCompleted } // Pending first, completed last
    }

    val groupedActivities = remember(filteredTodayActivities) {
        TimeOfDayUtils.groupActivitiesByTimeOfDay(filteredTodayActivities)
    }

    LaunchedEffect(Unit) {
        activityViewModel.loadTodayActivities()
    }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Today's Activities",
                subtitle = DateUtils.getCurrentDateFormatted(),
                icon = Icons.Default.DateRange,
                actions = {
                    IconButton(onClick = { activityViewModel.loadTodayActivities() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    FilterPanel(
                        selectedCategory = selectedCategory,
                        onCategoryChange = { selectedCategory = it },
                        selectedLocation = selectedLocation,
                        onLocationChange = { selectedLocation = it },
                        showTimeFilter = false, // TodayScreen doesn't need time filter
                        showStatusFilter = false // TodayScreen doesn't need status filter
                    )
                }

                item {
                    EnhancedProgressStatsCard(
                        completedCount = completedCount,
                        pendingCount = pendingCount,
                        totalCount = totalCount,
                        progressPercentage = progressPercentage
                    )
                }

                currentWeather?.let { weather ->
                    item {
                        CompactWeatherCard(weather = weather)
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
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                        }
                    }
                }

                if (groupedActivities.isNotEmpty() && !isLoading) {
                    groupedActivities.forEach { (timeOfDay, activities) ->
                        item {
                            EnhancedTimeOfDayHeader(timeOfDay, activities.size)
                        }

                        items(
                            items = activities,
                            key = { it.id }
                        ) { activity ->
                            ActivityItemCard(
                                activity = activity,
                                onToggleComplete = {
                                    activityViewModel.toggleActivityCompletion(activity)
                                    if (!activity.isCompleted) {
                                        showCelebration = true
                                    }
                                },
                                onEdit = {
                                    selectedActivity = activity
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedActivity = activity
                                    showDeleteDialog = true
                                },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }

                if (filteredTodayActivities.isEmpty() && !isLoading) {
                    item {
                        EmptyStateCard(onAddActivity = {
                            showAddDialog = true
                        })
                    }
                }

                if (filteredTodayActivities.isNotEmpty() && completedCount == totalCount) {
                    item {
                        AllCompletedCard()
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }

            if (showCelebration) {
                CelebrationOverlay()
                LaunchedEffect(Unit) {
                    delay(2000)
                    showCelebration = false
                }
            }
        }
    }

    if (showAddDialog) {
        AddActivityDialog(
            activity = null,
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

    if (showEditDialog && selectedActivity != null) {
        EditActivityDialog(
            activity = selectedActivity!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedActivity ->
                activityViewModel.updateActivity(updatedActivity)
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog && selectedActivity != null) {
        DeleteConfirmationDialog(
            activity = selectedActivity!!,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                activityViewModel.deleteActivity(selectedActivity!!)
                showDeleteDialog = false
            }
        )
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun EnhancedProgressStatsCard(
    completedCount: Int,
    pendingCount: Int,
    totalCount: Int,
    progressPercentage: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            LinearProgressIndicator(
                progress = progressPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("📊", "Total", totalCount.toString(), MaterialTheme.colorScheme.primary)
                StatItem("✅", "Done", completedCount.toString(), Color(0xFF4CAF50))
                StatItem("⏳", "Pending", pendingCount.toString(), Color(0xFFFF9800))
            }
        }
    }
}

@Composable
private fun StatItem(icon: String, label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun EnhancedTimeOfDayHeader(timeOfDay: String, count: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (timeOfDay.lowercase()) {
                                "morning" -> "🌅"
                                "afternoon" -> "☀️"
                                "evening" -> "🌆"
                                "night" -> "🌙"
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Text(
                    text = timeOfDay,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CelebrationOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "celebration")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            )
        )

        Card(
            modifier = Modifier
                .scale(scale)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Great Job!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}