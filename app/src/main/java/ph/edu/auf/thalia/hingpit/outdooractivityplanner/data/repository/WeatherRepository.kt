package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository

import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.interfaces.WeatherApiService
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.WeatherCache
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao.WeatherCacheDao
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.CurrentWeatherResponse
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.ForecastResponse

class WeatherRepository(
    private val api: WeatherApiService,
    private val weatherCacheDao: WeatherCacheDao
) {

    // -------------------------------------------------------
    // FETCH CURRENT WEATHER BY CITY (with local caching fallback)
    // -------------------------------------------------------
    suspend fun fetchCurrentByCity(city: String, apiKey: String): CurrentWeatherResponse? {
        return try {
            val response = api.currentByCity(city = city, apiKey = apiKey)

            // Save to cache
            cacheCurrentWeather(city, response)

            response
        } catch (e: Exception) {
            e.printStackTrace()

            // fallback → return cached weather (if any)
            getCached(city)
            null
        }
    }

    // -------------------------------------------------------
    // FETCH CURRENT WEATHER BY COORDINATES
    // -------------------------------------------------------
    suspend fun fetchCurrentByCoordinates(lat: Double, lon: Double, apiKey: String): CurrentWeatherResponse? {
        return try {
            val response = api.currentByCoordinates(lat = lat, lon = lon, apiKey = apiKey)

            // Save to cache using city name from response
            cacheCurrentWeather(response.city, response)

            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun cacheCurrentWeather(city: String, data: CurrentWeatherResponse) {
        val weatherCache = WeatherCache(
            city = city,
            temp = data.main.temp,
            condition = data.weather.firstOrNull()?.main ?: "",
            icon = data.weather.firstOrNull()?.icon ?: "",
            humidity = data.main.humidity,
            wind = data.wind.speed,
            cachedAt = System.currentTimeMillis()
        )

        weatherCacheDao.insertWeatherCache(weatherCache)
    }

    // -------------------------------------------------------
    // FETCH FORECAST (Free 5-day/3-hour API)
    // -------------------------------------------------------
    suspend fun fetchForecast(lat: Double, lon: Double, apiKey: String): ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.FiveDayForecastResponse? {
        return try {
            api.fiveDayForecast(lat = lat, lon = lon, apiKey = apiKey)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // -------------------------------------------------------
    // GET CACHED WEATHER (offline support)
    // -------------------------------------------------------
    suspend fun getCached(city: String): WeatherCache? {
        return weatherCacheDao.getWeatherCache(city)
    }
}