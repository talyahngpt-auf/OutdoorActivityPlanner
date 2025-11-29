package ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.CurrentWeatherResponse
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.ForecastResponse
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.WeatherRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.providers.LocationProvider

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    // Current weather state
    private val _currentWeather = MutableStateFlow<CurrentWeatherResponse?>(null)
    val currentWeather = _currentWeather.asStateFlow()

    // Forecast state
    private val _forecast = MutableStateFlow<ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.network.FiveDayForecastResponse?>(null)
    val forecast = _forecast.asStateFlow()

    // Location coordinates
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude = _longitude.asStateFlow()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Get current device location and fetch weather
    fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Clear previous weather data to show fresh fetch
                _currentWeather.value = null
                _forecast.value = null

                // Await location properly
                val location = locationProvider.getLastLocation()

                if (location != null) {
                    // Update coordinates
                    _latitude.value = location.latitude
                    _longitude.value = location.longitude

                    // Fetch weather with new coordinates
                    val response = repository.fetchCurrentByCoordinates(
                        lat = location.latitude,
                        lon = location.longitude,
                        apiKey = "d4a747d49224357d4fab7cc012649c8b"
                    )

                    if (response != null) {
                        _currentWeather.value = response

                        // Fetch forecast
                        val forecastResponse = repository.fetchForecast(
                            location.latitude,
                            location.longitude,
                            "d4a747d49224357d4fab7cc012649c8b"
                        )
                        _forecast.value = forecastResponse
                    } else {
                        _errorMessage.value = "Failed to fetch weather for your location"
                    }
                } else {
                    _errorMessage.value = "Unable to get location. Please check GPS settings."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fetch current weather by city name (for testing)
    fun getCurrentWeatherByCity(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Clear previous data
                _currentWeather.value = null
                _forecast.value = null

                // First, try with Philippines country code for common PH cities
                var response = if (isLikelyPhilippineCity(city)) {
                    repository.fetchCurrentByCity("$city,PH", apiKey)
                } else {
                    repository.fetchCurrentByCity(city, apiKey)
                }

                // If first attempt fails and city contains "City", try without it
                if (response == null && city.contains("City", ignoreCase = true)) {
                    val cityWithoutSuffix = city.replace("City", "", ignoreCase = true).trim()
                    _errorMessage.value = "Trying '$cityWithoutSuffix' instead..."
                    response = if (isLikelyPhilippineCity(cityWithoutSuffix)) {
                        repository.fetchCurrentByCity("$cityWithoutSuffix,PH", apiKey)
                    } else {
                        repository.fetchCurrentByCity(cityWithoutSuffix, apiKey)
                    }
                }

                // If still null and city doesn't have "City", try adding it
                if (response == null && !city.contains("City", ignoreCase = true)) {
                    val cityWithSuffix = "$city City"
                    _errorMessage.value = "Trying '$cityWithSuffix' instead..."
                    response = if (isLikelyPhilippineCity(cityWithSuffix)) {
                        repository.fetchCurrentByCity("$cityWithSuffix,PH", apiKey)
                    } else {
                        repository.fetchCurrentByCity(cityWithSuffix, apiKey)
                    }
                }

                if (response != null) {
                    _currentWeather.value = response
                    _errorMessage.value = null // Clear any "Trying..." message

                    // After getting current weather, fetch forecast using coordinates
                    val forecastResponse = repository.fetchForecast(
                        response.coord.lat,
                        response.coord.lon,
                        apiKey
                    )
                    _forecast.value = forecastResponse
                } else {
                    _errorMessage.value = "City '$city' not found. Try adding country code (e.g., 'Angeles,PH')."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper function to identify likely Philippine cities
    private fun isLikelyPhilippineCity(city: String): Boolean {
        val philippineCities = listOf(
            "manila", "quezon", "davao", "cebu", "zamboanga", "taguig", "pasig",
            "valenzuela", "antipolo", "cavite", "bacoor", "general santos", "parañaque",
            "las piñas", "makati", "bacolod", "muntinlupa", "san jose del monte",
            "iloilo", "cagayan de oro", "marikina", "pasay", "caloocan", "malabon",
            "navotas", "san juan", "mandaluyong", "baguio", "san fernando", "laoag",
            "dagupan", "urdaneta", "tuguegarao", "cauayan", "santiago", "ilagan",
            "angeles", "mabalacat", "san pablo", "tarlac", "olongapo", "cabanatuan",
            "gapan", "nueva ecija", "batangas", "lipa", "tanauan", "biñan", "calamba",
            "san pedro", "santa rosa", "cabuyao", "lucena", "naga", "legazpi",
            "sorsogon", "masbate", "tacloban", "ormoc", "maasin", "baybay", "borongan",
            "catbalogan", "calbayog", "roxas", "iloilo", "kalibo", "puerto princesa",
            "tagbilaran", "dumaguete", "bais", "canlaon", "butuan", "surigao",
            "tandag", "bislig", "cotabato", "koronadal", "tacurong", "kidapawan",
            "malaybalay", "valencia", "oroquieta", "ozamiz", "tangub", "dipolog",
            "dapitan", "pagadian", "isabela", "lamitan", "jolo", "bongao",
            "bacolor", "guagua", "mexico", "apalit", "arayat", "candaba", "lubao",
            "masantol", "minalin", "porac", "san fernando", "san luis", "san simon",
            "santa ana", "santa rita", "santo tomas", "sasmuan"
        )
        return philippineCities.any { city.lowercase().contains(it) }
    }

    // Get cached weather for offline support
    fun getCachedWeather(city: String) {
        viewModelScope.launch {
            try {
                val cached = repository.getCached(city)
                if (cached != null) {
                    // Convert cached data to display format if needed
                    _errorMessage.value = "Showing cached data for $city"
                } else {
                    _errorMessage.value = "No cached data available"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading cache: ${e.message}"
                e.printStackTrace()
            }
        }
    }
}