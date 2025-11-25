package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.CurrentWeatherResponse

@Composable
fun WeatherCard(weather: CurrentWeatherResponse) {
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // City Name
            Text(
                text = weather.city,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weather Icon (using emoji for now)
            Text(
                text = getWeatherEmoji(weather.weather.firstOrNull()?.main ?: "Clear"),
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Temperature
            Text(
                text = "${weather.main.temp.toInt()}°C",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )

            // Condition
            Text(
                text = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Weather Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetail(
                    icon = "💧",
                    label = "Humidity",
                    value = "${weather.main.humidity}%"
                )

                WeatherDetail(
                    icon = "💨",
                    label = "Wind",
                    value = "${weather.wind.speed} m/s"
                )

                WeatherDetail(
                    icon = "🌡️",
                    label = "Feels Like",
                    value = "${weather.main.feelsLike.toInt()}°C"
                )
            }
        }
    }
}

@Composable
private fun WeatherDetail(icon: String, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Helper function to get emoji based on weather condition
private fun getWeatherEmoji(condition: String): String {
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

