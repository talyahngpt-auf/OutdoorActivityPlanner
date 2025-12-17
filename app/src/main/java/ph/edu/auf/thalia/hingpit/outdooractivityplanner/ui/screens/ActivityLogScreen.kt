package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.navigation.NavHostController
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.*
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivityCategory
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.DateUtils
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.LocationType
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


    var selectedFilter by remember { mutableStateOf(FilterType.ALL) }
    var selectedStatus by remember { mutableStateOf(StatusFilter.ALL) }
    var selectedCategory by remember { mutableStateOf<ActivityCategory?>(null) }
    var selectedLocation by remember { mutableStateOf<LocationType?>(null) }

    var showDateRangeDialog by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<String?>(null) }
    var endDate by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ActivityEntity?>(null) }

    // Apply filters and sort: pending first, completed last
    val filteredActivities = remember(
        allActivities,
        selectedFilter,
        selectedStatus,
        startDate,
        endDate,
        selectedCategory,
        selectedLocation
    ) {
        applyFilters(
            allActivities,
            selectedFilter,
            selectedStatus,
            startDate,
            endDate,
            selectedCategory,
            selectedLocation
        ).sortedBy { it.isCompleted } // Pending first, completed last
    }

    // Group activities by date
    val groupedActivities = remember(filteredActivities, selectedFilter) {
        val grouped = filteredActivities.groupBy { it.date }
        when (selectedFilter) {
            FilterType.ALL -> grouped.toSortedMap()
            else -> grouped.toSortedMap(compareByDescending { it })
        }
    }

    // Calculate stats
    val totalCount = filteredActivities.size
    val completedCount = filteredActivities.count { it.isCompleted }
    val pendingCount = filteredActivities.count { !it.isCompleted }

    LaunchedEffect(Unit) {
        activityViewModel.loadAllActivities()
    }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Activity Log",
                subtitle = "Your complete activity history",
                icon = Icons.Default.CheckCircle,
                actions = {
                    IconButton(onClick = { activityViewModel.loadAllActivities() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
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
                    FilterPanel(
                        selectedCategory = selectedCategory,
                        onCategoryChange = { selectedCategory = it },
                        selectedLocation = selectedLocation,
                        onLocationChange = { selectedLocation = it },
                        showTimeFilter = true,
                        selectedTimeFilter = selectedFilter,
                        onTimeFilterChange = {
                            selectedFilter = it
                            if (it == FilterType.CUSTOM_RANGE) {
                                showDateRangeDialog = true
                            } else {
                                startDate = null
                                endDate = null
                            }
                        },
                        showStatusFilter = true,
                        selectedStatusFilter = selectedStatus,
                        onStatusFilterChange = { selectedStatus = it },
                        showDateRange = selectedFilter == FilterType.CUSTOM_RANGE,
                        startDate = startDate,
                        endDate = endDate,
                        onShowDateRangePicker = { showDateRangeDialog = true }
                    )
                }

                item {
                    EnhancedCompactStatsCard(
                        totalCount = totalCount,
                        completedCount = completedCount,
                        pendingCount = pendingCount
                    )
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
                    groupedActivities.forEach { (date, activities) ->
                        item {
                            EnhancedDateHeader(date = date, count = activities.size)
                        }

                        items(
                            items = activities,
                            key = { it.id }
                        ) { activity ->
                            ActivityItemCard(
                                activity = activity,
                                onToggleComplete = { activityViewModel.toggleActivityCompletion(activity) },
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

                if (filteredActivities.isEmpty() && !isLoading) {
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
                                    text = if (selectedFilter != FilterType.ALL || selectedStatus != StatusFilter.ALL) {
                                        "No activities match your current filters. Try adjusting your filter settings."
                                    } else {
                                        "You haven't logged any activities yet. Start planning from the home screen!"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                if (selectedFilter != FilterType.ALL || selectedStatus != StatusFilter.ALL) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    OutlinedButton(
                                        onClick = {
                                            selectedFilter = FilterType.ALL
                                            selectedStatus = StatusFilter.ALL
                                            selectedCategory = null
                                            selectedLocation = null
                                            startDate = null
                                            endDate = null
                                        },
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
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (showDateRangeDialog) {
        DateRangePickerDialog(
            onDismiss = { showDateRangeDialog = false },
            onConfirm = { start, end ->
                startDate = start
                endDate = end
                showDateRangeDialog = false
            },
            initialStartDate = startDate,
            initialEndDate = endDate
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
fun EnhancedCompactStatsCard(
    totalCount: Int,
    completedCount: Int,
    pendingCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedStatItem(
                    icon = "📋",
                    label = "Total",
                    value = totalCount.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                EnhancedStatItem(
                    icon = "✅",
                    label = "Done",
                    value = completedCount.toString(),
                    color = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                )
                EnhancedStatItem(
                    icon = "⏳",
                    label = "Pending",
                    value = pendingCount.toString(),
                    color = androidx.compose.ui.graphics.Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun EnhancedStatItem(
    icon: String,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun EnhancedDateHeader(date: String, count: Int) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val parsedDate = dateFormat.parse(date) ?: Date()
    val displayFormat = SimpleDateFormat("EEE (MMM. dd, yyyy)", Locale.getDefault())
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape
            ) {
                Text(
                    text = "$count ${if (count == 1) "activity" else "activities"}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    initialStartDate: String?,
    initialEndDate: String?
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var tempStartDate by remember { mutableStateOf(initialStartDate) }
    var tempEndDate by remember { mutableStateOf(initialEndDate) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date Range") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { showStartPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Start Date",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = if (tempStartDate != null) {
                                DateUtils.formatToMonthDay(tempStartDate!!)
                            } else {
                                "Select start date"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showEndPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = tempStartDate != null
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "End Date",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = if (tempEndDate != null) {
                                DateUtils.formatToMonthDay(tempEndDate!!)
                            } else {
                                "Select end date"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (tempStartDate != null && tempEndDate != null) {
                        onConfirm(tempStartDate!!, tempEndDate!!)
                    }
                },
                enabled = tempStartDate != null && tempEndDate != null
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showStartPicker) {
        val startPickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startPickerState.selectedDateMillis?.let { millis ->
                        tempStartDate = dateFormat.format(Date(millis))
                        if (tempEndDate != null && tempEndDate!! < tempStartDate!!) {
                            tempEndDate = null
                        }
                    }
                    showStartPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = startPickerState)
        }
    }

    if (showEndPicker) {
        val endPickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endPickerState.selectedDateMillis?.let { millis ->
                        val selectedEnd = dateFormat.format(Date(millis))
                        if (tempStartDate != null && selectedEnd >= tempStartDate!!) {
                            tempEndDate = selectedEnd
                        }
                    }
                    showEndPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = endPickerState)
        }
    }
}

enum class FilterType(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    LAST_WEEK("Last 7 Days"),
    CUSTOM_RANGE("Custom Range")
}

enum class StatusFilter(val label: String) {
    ALL("All"),
    COMPLETED("Completed"),
    PENDING("Pending")
}

fun buildFilterSummary(
    filterType: FilterType,
    statusFilter: StatusFilter,
    startDate: String? = null,
    endDate: String? = null
): String {
    val parts = mutableListOf<String>()

    if (filterType != FilterType.ALL) {
        if (filterType == FilterType.CUSTOM_RANGE && startDate != null && endDate != null) {
            parts.add("${DateUtils.formatToMonthDay(startDate)} - ${DateUtils.formatToMonthDay(endDate)}")
        } else {
            parts.add(filterType.label)
        }
    }

    if (statusFilter != StatusFilter.ALL) {
        parts.add(statusFilter.label)
    }

    return if (parts.isEmpty()) "No filters applied" else parts.joinToString(" • ")
}

fun applyFilters(
    activities: List<ActivityEntity>,
    filterType: FilterType,
    statusFilter: StatusFilter,
    startDate: String? = null,
    endDate: String? = null,
    category: ActivityCategory? = null,
    location: LocationType? = null
): List<ActivityEntity> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())
    val calendar = Calendar.getInstance()

    return activities.filter { activity ->
        val timeMatch = when (filterType) {
            FilterType.ALL -> true
            FilterType.TODAY -> activity.date == today
            FilterType.LAST_WEEK -> {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = dateFormat.format(calendar.time)
                activity.date >= weekAgo && activity.date <= today
            }
            FilterType.CUSTOM_RANGE -> {
                if (startDate != null && endDate != null) {
                    activity.date >= startDate && activity.date <= endDate
                } else true
            }
        }

        val statusMatch = when (statusFilter) {
            StatusFilter.ALL -> true
            StatusFilter.COMPLETED -> activity.isCompleted
            StatusFilter.PENDING -> !activity.isCompleted
        }

        val categoryMatch = category?.let { activity.category == it.toDisplayString() } ?: true
        val locationMatch = location?.let { activity.locationType == it.toDisplayString() } ?: true

        timeMatch && statusMatch && categoryMatch && locationMatch
    }
}