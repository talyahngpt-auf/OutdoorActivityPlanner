package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network

import com.squareup.moshi.Json

data class CurrentWeatherResponse(
    @Json(name = "name")
    val city: String,

    @Json(name = "weather")
    val weather: List<WeatherDescription>,

    @Json(name = "main")
    val main: MainInfo,

    @Json(name = "wind")
    val wind: WindInfo,

    @Json(name = "coord")
    val coord: Coordinates
)

data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String
)

data class MainInfo(
    val temp: Double,
    @Json(name = "feels_like")
    val feelsLike: Double,
    val humidity: Int
)

data class WindInfo(
    val speed: Double
)

data class Coordinates(
    val lat: Double,
    val lon: Double
)
