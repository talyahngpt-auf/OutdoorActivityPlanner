package ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.interfaces

import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.CurrentWeatherResponse
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.FiveDayForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    // Fetch weather by city name
    @GET("weather")
    suspend fun currentByCity(
        @Query("q") city: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): CurrentWeatherResponse

    // Fetch weather by coordinates (lat/lon)
    @GET("weather")
    suspend fun currentByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): CurrentWeatherResponse

    @GET("forecast")
    suspend fun fiveDayForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): FiveDayForecastResponse
}