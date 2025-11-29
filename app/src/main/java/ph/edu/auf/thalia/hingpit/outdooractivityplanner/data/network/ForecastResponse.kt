package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("lat")
    val latitude: Double,

    @SerializedName("lon")
    val longitude: Double,

    @SerializedName("timezone")
    val timezone: String,

    @SerializedName("daily")
    val daily: List<DailyForecast>
)

data class DailyForecast(
    @SerializedName("dt")
    val dt: Long, // timestamp

    @SerializedName("temp")
    val temperature: TemperatureInfo,

    @SerializedName("weather")
    val weather: List<WeatherDescription>,

    @SerializedName("humidity")
    val humidity: Int,

    @SerializedName("wind_speed")
    val windSpeed: Double
)

data class TemperatureInfo(
    @SerializedName("day")
    val day: Double,

    @SerializedName("min")
    val min: Double,

    @SerializedName("max")
    val max: Double
)
