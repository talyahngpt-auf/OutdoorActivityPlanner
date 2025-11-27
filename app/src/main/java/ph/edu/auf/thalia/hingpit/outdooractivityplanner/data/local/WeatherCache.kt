package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "weather_cache")
data class WeatherCache(
    @PrimaryKey
    val city: String = "",
    val temp: Double = 0.0,
    val condition: String = "",
    val icon: String = "",
    val humidity: Int = 0,
    val wind: Double = 0.0,
    val cachedAt: Long = System.currentTimeMillis()
)
