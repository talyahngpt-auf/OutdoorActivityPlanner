package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.DailyForecastSummary
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivitySuggestion
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivitySuggestions
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SimpleForecastCard(
    dailySummary: DailyForecastSummary,
    modifier: Modifier = Modifier
) {
    val dayOfWeek = DateUtils.formatToShortDay(dailySummary.date)

    Card(
        modifier = modifier.width(100.dp),
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

@Composable
fun ExpandedForecastCard(
    dailyForecast: DailyForecastSummary,
    onActivityClick: (ActivitySuggestion, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = dateFormat.parse(dailyForecast.date) ?: Date()
    val dayOfWeek = DateUtils.formatToDayOfWeek(dailyForecast.date)
    val dateString = DateUtils.formatToMonthDay(dailyForecast.date)

    val condition = dailyForecast.weather.firstOrNull()?.main ?: "Clear"
    val suggestions = ActivitySuggestions.getSuggestions(condition, dailyForecast.avgTemp, limit = 4)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
private fun ForecastWeatherDetailItem(
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