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

    // Current weather state - Changed to StateFlow for consistency
    private val _currentWeather = MutableStateFlow<CurrentWeatherResponse?>(null)
    val currentWeather = _currentWeather.asStateFlow()

    // Forecast state
    private val _forecast = MutableStateFlow<ForecastResponse?>(null)
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

    // Get current device location
    fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                locationProvider.getLastLocation { location ->
                    if (location != null) {
                        _latitude.value = location.latitude
                        _longitude.value = location.longitude

                        // Automatically fetch weather after getting location
                        getCurrentWeatherByCoordinates()
                    } else {
                        _errorMessage.value = "Unable to get location"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error getting location: ${e.message}"
                _isLoading.value = false
                e.printStackTrace()
            }
        }
    }

    // Fetch current weather by coordinates (device location)
    fun getCurrentWeatherByCoordinates() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val lat = _latitude.value
                val lon = _longitude.value

                if (lat != null && lon != null) {
                    val response = repository.fetchCurrentByCoordinates(
                        lat = lat,
                        lon = lon,
                        apiKey = "d4a747d49224357d4fab7cc012649c8b" // Using your API key
                    )

                    if (response != null) {
                        _currentWeather.value = response

                        // After getting current weather, fetch forecast using coordinates
                        fetchForecast(lat, lon, "d4a747d49224357d4fab7cc012649c8b")
                    } else {
                        _errorMessage.value = "Failed to fetch weather data for your location"
                    }
                } else {
                    _errorMessage.value = "Location coordinates not available"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching weather: ${e.message}"
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

                var response = repository.fetchCurrentByCity(city, apiKey)

                // If first attempt fails and city contains "City", try without it
                if (response == null && city.contains("City", ignoreCase = true)) {
                    val cityWithoutSuffix = city.replace("City", "", ignoreCase = true).trim()
                    _errorMessage.value = "Trying '$cityWithoutSuffix' instead..."
                    response = repository.fetchCurrentByCity(cityWithoutSuffix, apiKey)
                }

                // If still null and city doesn't have "City", try adding it
                if (response == null && !city.contains("City", ignoreCase = true)) {
                    val cityWithSuffix = "$city City"
                    _errorMessage.value = "Trying '$cityWithSuffix' instead..."
                    response = repository.fetchCurrentByCity(cityWithSuffix, apiKey)
                }

                if (response != null) {
                    _currentWeather.value = response
                    _errorMessage.value = null // Clear any "Trying..." message

                    // After getting current weather, fetch forecast using coordinates
                    fetchForecast(response.coord.lat, response.coord.lon, apiKey)
                } else {
                    _errorMessage.value = "City '$city' not found."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fetch forecast data
    fun fetchForecast(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = repository.fetchForecast(lat, lon, apiKey)
                _forecast.value = response
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching forecast: ${e.message}"
                e.printStackTrace()
            }
        }
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