package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.BottomNavigationBar
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.ActivityViewModel
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*


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


    // Dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ActivityEntity?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }


    // Celebration animation state
    var showCelebration by remember { mutableStateOf(false) }


    // Calculate stats
    val completedCount = todayActivities.count { it.isCompleted }
    val pendingCount = todayActivities.count { !it.isCompleted }
    val totalCount = todayActivities.size
    val progressPercentage = if (totalCount > 0) (completedCount.toFloat() / totalCount) * 100 else 0f


    // Group activities by time of day
    val groupedActivities = remember(todayActivities) {
        groupActivitiesByTimeOfDay(todayActivities)
    }


    // Reload activities when screen is opened
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
                            text = getCurrentDateFormatted(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    // Refresh button
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


                // Progress Stats Card
                item {
                    ProgressStatsCard(
                        completedCount = completedCount,
                        pendingCount = pendingCount,
                        totalCount = totalCount,
                        progressPercentage = progressPercentage
                    )
                }


                // Current Weather Card (compact)
                currentWeather?.let { weather ->
                    item {
                        CompactWeatherCard(weather = weather)
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


                // Activities grouped by time
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


                // Empty State
                if (todayActivities.isEmpty() && !isLoading) {
                    item {
                        EmptyStateCard(onAddActivity = {
                            navController.navigate("home")
                        })
                    }
                }


                // Motivational message at the bottom
                if (todayActivities.isNotEmpty() && completedCount == totalCount) {
                    item {
                        AllCompletedCard()
                    }
                }


                item { Spacer(modifier = Modifier.height(16.dp)) }
            }


            // Celebration Animation Overlay
            if (showCelebration) {
                CelebrationOverlay()
                LaunchedEffect(Unit) {
                    delay(2000)
                    showCelebration = false
                }
            }
        }
    }


    // Edit Dialog
    if (showEditDialog && selectedActivity != null) {
        EditActivityDialog(
            activity = selectedActivity!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedActivity ->
                activityViewModel.updateActivity(updatedActivity)
                showEditDialog = false
            },
            onShowEmojiPicker = { showEmojiPicker = true }
        )
    }


    // Delete Confirmation Dialog
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


    // Native Emoji Picker (Alternative approach)
    if (showEmojiPicker) {
        EmojiPickerDialog(
            onDismiss = { showEmojiPicker = false },
            onEmojiSelected = { emoji ->
                // Handle emoji selection
                showEmojiPicker = false
            }
        )
    }
}


// Helper function to get current date formatted
fun getCurrentDateFormatted(): String {
    val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}


// Helper function to group activities by time of day
fun groupActivitiesByTimeOfDay(activities: List<ActivityEntity>): Map<String, List<ActivityEntity>> {
    return activities.groupBy { activity ->
        val time = activity.time
        val hour = time.split(":").firstOrNull()?.toIntOrNull() ?: 12


        when (hour) {
            in 5..11 -> "🌅 Morning"
            in 12..17 -> "☀️ Afternoon"
            in 18..21 -> "🌆 Evening"
            else -> "🌙 Night"
        }
    }.toSortedMap(compareBy { timeOfDay ->
        when {
            timeOfDay.contains("Morning") -> 1
            timeOfDay.contains("Afternoon") -> 2
            timeOfDay.contains("Evening") -> 3
            else -> 4
        }
    })
}


@Composable
fun ProgressStatsCard(
    completedCount: Int,
    pendingCount: Int,
    totalCount: Int,
    progressPercentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )


                Text(
                    text = "${progressPercentage.toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }


            Spacer(modifier = Modifier.height(12.dp))


            // Progress Bar
            LinearProgressIndicator(
                progress = progressPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )


            Spacer(modifier = Modifier.height(16.dp))


            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = "✅",
                    label = "Completed",
                    value = completedCount.toString(),
                    color = Color(0xFF4CAF50)
                )


                StatItem(
                    icon = "⏳",
                    label = "Pending",
                    value = pendingCount.toString(),
                    color = Color(0xFFFF9800)
                )


                StatItem(
                    icon = "📋",
                    label = "Total",
                    value = totalCount.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
fun StatItem(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}


@Composable
fun CompactWeatherCard(weather: ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.CurrentWeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = getWeatherEmoji(weather.weather.firstOrNull()?.main ?: "Clear"),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Current Weather",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${weather.main.temp.toInt()}°C • ${weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


fun getWeatherEmoji(condition: String): String {
    return when (condition.lowercase()) {
        "clear" -> "☀️"
        "clouds" -> "☁️"
        "rain" -> "🌧️"
        "drizzle" -> "🌦️"
        "thunderstorm" -> "⛈️"
        "snow" -> "❄️"
        "mist", "fog" -> "🌫️"
        else -> "🌤️"
    }
}


@Composable
fun TimeOfDayHeader(timeOfDay: String, count: Int) {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityItemCard(
    activity: ActivityEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )


    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (activity.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (activity.isCompleted) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = activity.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4CAF50)
                )
            )


            Spacer(modifier = Modifier.width(12.dp))


            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (activity.isCompleted) TextDecoration.LineThrough else null,
                    color = if (activity.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )


                if (activity.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2
                    )
                }


                Spacer(modifier = Modifier.height(8.dp))


                // Time and Weather chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "🕐 ${activity.time}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }


                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${getWeatherEmoji(activity.weatherCondition)} ${activity.weatherCondition}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }


            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }


                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun EmptyStateCard(onAddActivity: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📅",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Activities Planned",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start planning your day by adding activities from the home screen!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddActivity,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Activity")
            }
        }
    }
}


@Composable
fun AllCompletedCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "All Done!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "You've completed all your activities for today!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Composable
fun CelebrationOverlay() {
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


// Edit Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityDialog(
    activity: ActivityEntity,
    onDismiss: () -> Unit,
    onConfirm: (ActivityEntity) -> Unit,
    onShowEmojiPicker: () -> Unit
) {
    var title by remember { mutableStateOf(activity.title) }
    var description by remember { mutableStateOf(activity.description) }
    var time by remember { mutableStateOf(activity.time) }


    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = time.split(":")[0].toInt(),
        initialMinute = time.split(":")[1].toInt()
    )


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Activity") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )


                Spacer(modifier = Modifier.height(12.dp))


                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )


                Spacer(modifier = Modifier.height(12.dp))


                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Time: $time")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        activity.copy(
                            title = title,
                            description = description,
                            time = time
                        )
                    )
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


    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = String.format(
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


@Composable
fun DeleteConfirmationDialog(
    activity: ActivityEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Delete Activity?") },
        text = {
            Text("Are you sure you want to delete \"${activity.title}\"? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun EmojiPickerDialog(
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,  // Changed from 'onDismiss'
        title = { Text("Choose Emoji") },
        text = {
            Text("Use your device's emoji keyboard to select an emoji, or we can implement a custom picker.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

