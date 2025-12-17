package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network

import com.google.gson.annotations.SerializedName

data class FiveDayForecastResponse(
    @SerializedName("list")
    val list: List<ForecastItem>,

    @SerializedName("city")
    val city: CityInfo
)

data class ForecastItem(
    @SerializedName("dt")
    val dt: Long, // timestamp

    @SerializedName("main")
    val main: MainInfo,

    @SerializedName("weather")
    val weather: List<WeatherDescription>,

    @SerializedName("wind")
    val wind: WindInfo,

    @SerializedName("dt_txt")
    val dtTxt: String
)

data class CityInfo(
    @SerializedName("name")
    val name: String,

    @SerializedName("country")
    val country: String
)

// Helper function to group forecasts by day
fun FiveDayForecastResponse.getDailyForecasts(): List<DailyForecastSummary> {
    val dailyMap = mutableMapOf<String, MutableList<ForecastItem>>()

    // Group by date
    list.forEach { item ->
        val date = item.dtTxt.substring(0, 10) // "2024-07-17"
        dailyMap.getOrPut(date) { mutableListOf() }.add(item)
    }

    // Convert to daily summaries
    return dailyMap.entries.take(5).map { (date, items) ->
        val temps = items.map { it.main.temp }
        val noonForecast = items.find { it.dtTxt.contains("12:00:00") } ?: items[items.size / 2]

        DailyForecastSummary(
            date = date,
            timestamp = items.first().dt,
            minTemp = temps.minOrNull() ?: 0.0,
            maxTemp = temps.maxOrNull() ?: 0.0,
            avgTemp = temps.average(),
            weather = noonForecast.weather,
            humidity = items.map { it.main.humidity }.average().toInt(),
            windSpeed = items.map { it.wind.speed }.average()
        )
    }
}

data class DailyForecastSummary(
    val date: String,
    val timestamp: Long,
    val minTemp: Double,
    val maxTemp: Double,
    val avgTemp: Double,
    val weather: List<WeatherDescription>,
    val humidity: Int,
    val windSpeed: Double
)