package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network

import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    @SerializedName("name")
    val city: String,

    @SerializedName("weather")
    val weather: List<WeatherDescription>,

    @SerializedName("main")
    val main: MainInfo,

    @SerializedName("wind")
    val wind: WindInfo,

    @SerializedName("coord")
    val coord: Coordinates
)

data class WeatherDescription(
    @SerializedName("main")
    val main: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("icon")
    val icon: String
)

data class MainInfo(
    @SerializedName("temp")
    val temp: Double,

    @SerializedName("feels_like")
    val feelsLike: Double,

    @SerializedName("humidity")
    val humidity: Int
)

data class WindInfo(
    @SerializedName("speed")
    val speed: Double
)

data class Coordinates(
    @SerializedName("lat")
    val lat: Double,

    @SerializedName("lon")
    val lon: Double
)
