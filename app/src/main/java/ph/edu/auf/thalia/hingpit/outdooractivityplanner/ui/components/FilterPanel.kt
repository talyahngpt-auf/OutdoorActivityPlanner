package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.FilterType
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.StatusFilter
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivityCategory
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.DateUtils
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.LocationType

@Composable
fun FilterPanel(
    selectedCategory: ActivityCategory?,
    onCategoryChange: (ActivityCategory?) -> Unit,
    selectedLocation: LocationType?,
    onLocationChange: (LocationType?) -> Unit,

    showTimeFilter: Boolean = false,
    selectedTimeFilter: FilterType? = null,
    onTimeFilterChange: ((FilterType) -> Unit)? = null,

    showStatusFilter: Boolean = false,
    selectedStatusFilter: StatusFilter? = null,
    onStatusFilterChange: ((StatusFilter) -> Unit)? = null,

    showDateRange: Boolean = false,
    startDate: String? = null,
    endDate: String? = null,
    onShowDateRangePicker: (() -> Unit)? = null,

    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            onClick = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (showTimeFilter && selectedTimeFilter != null && onTimeFilterChange != null) {
                        DropdownSection(
                            title = "Time Period",
                            options = FilterType.values().map { it.label },
                            selectedOption = selectedTimeFilter.label,
                            onOptionSelected = { label ->
                                FilterType.values().find { it.label == label }?.let { onTimeFilterChange(it) }
                            }
                        )

                        if (selectedTimeFilter == FilterType.CUSTOM_RANGE && onShowDateRangePicker != null) {
                            FilledTonalButton(
                                onClick = onShowDateRangePicker,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (startDate != null && endDate != null) {
                                        "${DateUtils.formatToMonthDay(startDate)} - ${DateUtils.formatToMonthDay(endDate)}"
                                    } else {
                                        "Select date range"
                                    }
                                )
                            }
                        }
                    }

                    if (showStatusFilter && selectedStatusFilter != null && onStatusFilterChange != null) {
                        DropdownSection(
                            title = "Status",
                            options = StatusFilter.values().map { it.label },
                            selectedOption = selectedStatusFilter.label,
                            onOptionSelected = { label ->
                                StatusFilter.values().find { it.label == label }?.let { onStatusFilterChange(it) }
                            }
                        )
                    }

                    DropdownSection(
                        title = "Category",
                        options = ActivityCategory.values().map { it.toDisplayString() },
                        selectedOption = selectedCategory?.toDisplayString(),
                        onOptionSelected = { label ->
                            val category = ActivityCategory.values().find { it.toDisplayString() == label }
                            onCategoryChange(category)
                        }
                    )

                    DropdownSection(
                        title = "Location",
                        options = LocationType.values().map { it.toDisplayString() },
                        selectedOption = selectedLocation?.toDisplayString(),
                        onOptionSelected = { label ->
                            val location = LocationType.values().find { it.toDisplayString() == label }
                            onLocationChange(location)
                        }
                    )

                    OutlinedButton(
                        onClick = {
                            onCategoryChange(null)
                            onLocationChange(null)
                            onTimeFilterChange?.invoke(FilterType.ALL)
                            onStatusFilterChange?.invoke(StatusFilter.ALL)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Filters")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSection(
    title: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedOption ?: "Select $title",
                onValueChange = {},
                readOnly = true,
                label = { Text(title) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
