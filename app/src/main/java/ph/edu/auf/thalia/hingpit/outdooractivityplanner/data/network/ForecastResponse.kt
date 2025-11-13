package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network

import com.squareup.moshi.Json

data class ForecastResponse(
    @Json(name = "lat")
    val latitude: Double,

    @Json(name = "lon")
    val longitude: Double,

    @Json(name = "timezone")
    val timezone: String,

    @Json(name = "daily")
    val daily: List<DailyForecast>
)

data class DailyForecast(
    val dt: Long, // timestamp

    @Json(name = "temp")
    val temperature: TemperatureInfo,

    @Json(name = "weather")
    val weather: List<WeatherDescription>,

    val humidity: Int,
    val wind_speed: Double
)

data class TemperatureInfo(
    val day: Double,
    val min: Double,
    val max: Double
)
