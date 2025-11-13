package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.interfaces.WeatherApiService
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.WeatherCache
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.CurrentWeatherResponse
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.ForecastResponse

class WeatherRepository(
    private val api: WeatherApiService,
    private val realm: Realm
) {

    // -------------------------------------------------------
    // FETCH CURRENT WEATHER (with local caching fallback)
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

    private suspend fun cacheCurrentWeather(city: String, data: CurrentWeatherResponse) {
        realm.write {
            val existing = query<WeatherCache>("city == $0", city).first().find()
            if (existing != null) delete(existing)

            copyToRealm(
                WeatherCache().apply {
                    this.city = city
                    this.temp = data.main.temp
                    this.condition = data.weather.firstOrNull()?.main ?: ""
                    this.icon = data.weather.firstOrNull()?.icon ?: ""
                    this.humidity = data.main.humidity
                    this.wind = data.wind.speed
                }
            )
        }
    }

    // -------------------------------------------------------
    // FETCH FORECAST (OneCall API)
    // -------------------------------------------------------
    suspend fun fetchForecast(lat: Double, lon: Double, apiKey: String): ForecastResponse? {
        return try {
            api.oneCall(lat = lat, lon = lon, apiKey = apiKey)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // -------------------------------------------------------
    // GET CACHED WEATHER (offline support)
    // -------------------------------------------------------
    fun getCached(city: String): WeatherCache? {
        return realm.query<WeatherCache>("city == $0", city).first().find()
    }
}
