package ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.interfaces

import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.CurrentWeatherResponse
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun currentByCity(
        @Query("q") city: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): CurrentWeatherResponse

    @GET("onecall")
    suspend fun oneCall(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String = "minutely,hourly,alerts",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): ForecastResponse
}
