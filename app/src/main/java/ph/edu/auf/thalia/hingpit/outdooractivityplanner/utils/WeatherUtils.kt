package ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils

object WeatherUtils {
    fun getWeatherIconUrl(iconCode: String): String {
        return "https://openweathermap.org/img/wn/${iconCode}@2x.png"
    }
    fun formatTemperature(temp: Double): String {
        return "${temp.toInt()}°C"
    }

    fun formatWindSpeed(speed: Double): String {
        return "${speed.toInt()} m/s"
    }

    fun formatHumidity(humidity: Int): String {
        return "$humidity%"
    }

    fun formatWeatherDescription(description: String): String {
        return description.replaceFirstChar { it.uppercase() }
    }
}