package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.*
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.DateUtils
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

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ActivityEntity?>(null) }

    var showCelebration by remember { mutableStateOf(false) }

    val completedCount = todayActivities.count { it.isCompleted }
    val pendingCount = todayActivities.count { !it.isCompleted }
    val totalCount = todayActivities.size
    val progressPercentage = if (totalCount > 0) (completedCount.toFloat() / totalCount) * 100 else 0f

    val groupedActivities = remember(todayActivities) {
        TimeOfDayUtils.groupActivitiesByTimeOfDay(todayActivities)
    }

    LaunchedEffect(Unit) {
        activityViewModel.loadTodayActivities()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today's Activities")
                        Text(
                            text = DateUtils.getCurrentDateFormatted(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        activityViewModel.loadTodayActivities()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    ProgressStatsCard(
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
                            CircularProgressIndicator()
                        }
                    }
                }

                if (groupedActivities.isNotEmpty() && !isLoading) {
                    groupedActivities.forEach { (timeOfDay, activities) ->
                        item {
                            TimeOfDayHeader(timeOfDay, activities.size)
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

                if (todayActivities.isEmpty() && !isLoading) {
                    item {
                        EmptyStateCard(onAddActivity = {
                            navController.navigate("home")
                        })
                    }
                }

                if (todayActivities.isNotEmpty() && completedCount == totalCount) {
                    item {
                        AllCompletedCard()
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
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
private fun TimeOfDayHeader(timeOfDay: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timeOfDay,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            shape = CircleShape
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun CelebrationOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            )
        )

        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.scale(scale)
        )
    }
}