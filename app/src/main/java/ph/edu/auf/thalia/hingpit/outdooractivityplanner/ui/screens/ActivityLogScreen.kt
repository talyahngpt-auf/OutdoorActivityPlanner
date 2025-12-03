package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.FlowRow
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.BottomNavigationBar
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.DateUtils
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.WeatherUtils
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.ActivityViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActivityLogScreen(
    navController: NavHostController,
    activityViewModel: ActivityViewModel
) {
    val allActivities by activityViewModel.activities.collectAsState()
    val isLoading by activityViewModel.isLoading.collectAsState()

    // Filter states
    var selectedFilter by remember { mutableStateOf(FilterType.ALL) }
    var selectedStatus by remember { mutableStateOf(StatusFilter.ALL) }
    var showFilterMenu by remember { mutableStateOf(false) }

    // Date picker state for specific date filter
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedSpecificDate by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ActivityEntity?>(null) }

    // Expanded state for each activity
    var expandedActivities by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Apply filters
    val filteredActivities = remember(allActivities, selectedFilter, selectedStatus, selectedSpecificDate) {
        applyFilters(allActivities, selectedFilter, selectedStatus, selectedSpecificDate)
    }

    // Group activities by date with proper ordering
    val groupedActivities = remember(filteredActivities, selectedFilter) {
        val grouped = filteredActivities.groupBy { it.date }

        // Sort based on filter type
        when (selectedFilter) {
            FilterType.ALL -> grouped.toSortedMap() // Ascending order for ALL
            else -> grouped.toSortedMap(compareByDescending { it }) // Descending for others
        }
    }

    // Calculate stats
    val totalCount = filteredActivities.size
    val completedCount = filteredActivities.count { it.isCompleted }
    val pendingCount = filteredActivities.count { !it.isCompleted }

    // Load activities when screen opens
    LaunchedEffect(Unit) {
        activityViewModel.loadAllActivities()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Log") },
                actions = {
                    // Refresh button
                    IconButton(onClick = { activityViewModel.loadAllActivities() }) {
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

                // Prominent Filter Button
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showFilterMenu = !showFilterMenu }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Filter",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Filters",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (selectedFilter != FilterType.ALL || selectedStatus != StatusFilter.ALL || selectedSpecificDate != null) {
                                        Text(
                                            text = buildFilterSummary(selectedFilter, selectedStatus, selectedSpecificDate),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Icon(
                                if (showFilterMenu) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showFilterMenu) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Filter Chips
                item {
                    AnimatedVisibility(
                        visible = showFilterMenu,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Time Filter
                            Text(
                                text = "Time Period",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilterType.values().forEach { filter ->
                                    FilterChip(
                                        selected = selectedFilter == filter,
                                        onClick = {
                                            selectedFilter = filter
                                            if (filter != FilterType.SPECIFIC_DATE) {
                                                selectedSpecificDate = null
                                            }
                                        },
                                        label = { Text(filter.label) },
                                        leadingIcon = if (selectedFilter == filter) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                        } else null
                                    )
                                }
                            }

                            // Date picker button for specific date
                            if (selectedFilter == FilterType.SPECIFIC_DATE) {
                                OutlinedButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedSpecificDate != null) {
                                            DateUtils.formatToMonthDay(selectedSpecificDate!!)
                                        } else {
                                            "Select a date"
                                        }
                                    )
                                }
                            }

                            // Status Filter
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatusFilter.values().forEach { status ->
                                    FilterChip(
                                        selected = selectedStatus == status,
                                        onClick = { selectedStatus = status },
                                        label = { Text(status.label) },
                                        leadingIcon = if (selectedStatus == status) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                        } else null
                                    )
                                }
                            }

                            HorizontalDivider()
                        }
                    }
                }

                // Compact Stats Summary Card
                item {
                    CompactStatsCard(
                        totalCount = totalCount,
                        completedCount = completedCount,
                        pendingCount = pendingCount
                    )
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

                // Activity entries grouped by date
                if (groupedActivities.isNotEmpty() && !isLoading) {
                    groupedActivities.forEach { (date, activities) ->
                        item {
                            DateHeader(date = date, count = activities.size)
                        }

                        items(
                            items = activities,
                            key = { it.id }
                        ) { activity ->
                            CompactActivityLogItem(
                                activity = activity,
                                isExpanded = expandedActivities.contains(activity.id),
                                onToggleExpand = {
                                    expandedActivities = if (expandedActivities.contains(activity.id)) {
                                        expandedActivities - activity.id
                                    } else {
                                        expandedActivities + activity.id
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
                                onToggleComplete = {
                                    activityViewModel.toggleActivityCompletion(activity)
                                },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }

                // Empty State
                if (filteredActivities.isEmpty() && !isLoading) {
                    item {
                        EmptyLogState(
                            filterType = selectedFilter,
                            statusFilter = selectedStatus,
                            onResetFilters = {
                                selectedFilter = FilterType.ALL
                                selectedStatus = StatusFilter.ALL
                                selectedSpecificDate = null
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        selectedSpecificDate = dateFormat.format(Date(millis))
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

    // Edit Dialog
    if (showEditDialog && selectedActivity != null) {
        EditActivityLogDialog(
            activity = selectedActivity!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedActivity ->
                activityViewModel.updateActivity(updatedActivity)
                showEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedActivity != null) {
        DeleteConfirmationLogDialog(
            activity = selectedActivity!!,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                activityViewModel.deleteActivity(selectedActivity!!)
                showDeleteDialog = false
            }
        )
    }
}

// Helper function to build filter summary text
fun buildFilterSummary(
    filterType: FilterType,
    statusFilter: StatusFilter,
    specificDate: String?
): String {
    val parts = mutableListOf<String>()

    if (filterType != FilterType.ALL) {
        if (filterType == FilterType.SPECIFIC_DATE && specificDate != null) {
            parts.add(DateUtils.formatToMonthDay(specificDate))
        } else {
            parts.add(filterType.label)
        }
    }

    if (statusFilter != StatusFilter.ALL) {
        parts.add(statusFilter.label)
    }

    return if (parts.isEmpty()) "No filters applied" else parts.joinToString(" • ")
}

// Filter enums
enum class FilterType(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    LAST_WEEK("Last 7 Days"),
    FUTURE("Future"),
    SPECIFIC_DATE("Specific Date")
}

enum class StatusFilter(val label: String) {
    ALL("All"),
    COMPLETED("Completed"),
    PENDING("Pending")
}

// Apply filters function
fun applyFilters(
    activities: List<ActivityEntity>,
    filterType: FilterType,
    statusFilter: StatusFilter,
    specificDate: String?
): List<ActivityEntity> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())
    val calendar = Calendar.getInstance()

    return activities.filter { activity ->
        // Time filter
        val timeMatch = when (filterType) {
            FilterType.ALL -> true
            FilterType.TODAY -> activity.date == today
            FilterType.LAST_WEEK -> {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = dateFormat.format(calendar.time)
                activity.date >= weekAgo && activity.date <= today
            }
            FilterType.FUTURE -> activity.date > today
            FilterType.SPECIFIC_DATE -> activity.date == (specificDate ?: "")
        }

        // Status filter
        val statusMatch = when (statusFilter) {
            StatusFilter.ALL -> true
            StatusFilter.COMPLETED -> activity.isCompleted
            StatusFilter.PENDING -> !activity.isCompleted
        }

        timeMatch && statusMatch
    }
}

@Composable
fun CompactStatsCard(
    totalCount: Int,
    completedCount: Int,
    pendingCount: Int
) {
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
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CompactStatItem(
                icon = "📋",
                label = "Total",
                value = totalCount.toString(),
                color = MaterialTheme.colorScheme.primary
            )

            CompactStatItem(
                icon = "✅",
                label = "Done",
                value = completedCount.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )

            CompactStatItem(
                icon = "⏳",
                label = "Pending",
                value = pendingCount.toString(),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun CompactStatItem(
    icon: String,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DateHeader(date: String, count: Int) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val parsedDate = dateFormat.parse(date) ?: Date()
    val displayFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    val today = dateFormat.format(Date())

    val displayText = when (date) {
        today -> "Today"
        else -> {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = dateFormat.format(calendar.time)
            if (date == yesterday) "Yesterday" else displayFormat.format(parsedDate)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = "$count ${if (count == 1) "activity" else "activities"}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun CompactActivityLogItem(
    activity: ActivityEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
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
        Column(modifier = Modifier.fillMaxWidth()) {
            // Condensed view (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                Checkbox(
                    checked = activity.isCompleted,
                    onCheckedChange = { onToggleComplete() }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Title only
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (activity.isCompleted) TextDecoration.LineThrough else null,
                        color = if (activity.isCompleted)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    // Show time chip when condensed
                    if (!isExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🕐 ${activity.time}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Expand/collapse arrow
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Expanded view
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp, end = 12.dp, bottom = 12.dp)
                ) {
                    // Description
                    if (activity.description.isNotBlank()) {
                        Text(
                            text = activity.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Info chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = "🕐 ${activity.time}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Weather with icon
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AsyncImage(
                                    model = WeatherUtils.getWeatherIconUrl(activity.weatherIconCode),
                                    contentDescription = activity.weatherCondition,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = activity.weatherCondition,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLogState(
    filterType: FilterType,
    statusFilter: StatusFilter,
    onResetFilters: () -> Unit
) {
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
                text = "📭",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Activities Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (filterType != FilterType.ALL || statusFilter != StatusFilter.ALL) {
                    "No activities match your current filters. Try adjusting your filter settings."
                } else {
                    "You haven't logged any activities yet. Start planning from the home screen!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (filterType != FilterType.ALL || statusFilter != StatusFilter.ALL) {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onResetFilters,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Filters")
                }
            }
        }
    }
}

// Dialogs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityLogDialog(
    activity: ActivityEntity,
    onDismiss: () -> Unit,
    onConfirm: (ActivityEntity) -> Unit
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
fun DeleteConfirmationLogDialog(
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