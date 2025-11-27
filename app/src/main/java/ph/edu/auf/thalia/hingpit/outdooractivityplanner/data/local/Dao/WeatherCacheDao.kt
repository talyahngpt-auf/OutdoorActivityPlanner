package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao


import androidx.room.*
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.WeatherCache


@Dao
interface WeatherCacheDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(weatherCache: WeatherCache)


    @Query("SELECT * FROM weather_cache WHERE city = :city LIMIT 1")
    suspend fun getWeatherCache(city: String): WeatherCache?


    @Query("SELECT * FROM weather_cache")
    suspend fun getAllWeatherCache(): List<WeatherCache>


    @Delete
    suspend fun deleteWeatherCache(weatherCache: WeatherCache)


    @Query("DELETE FROM weather_cache WHERE city = :city")
    suspend fun deleteWeatherCacheByCity(city: String)


    @Query("DELETE FROM weather_cache")
    suspend fun deleteAllWeatherCache()
}
