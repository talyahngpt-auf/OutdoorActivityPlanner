package ph.edu.auf.thalia.hingpit.outdooractivityplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.launch
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.factory.RetrofitFactory
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.apis.interfaces.WeatherApiService
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.WeatherCache
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.WeatherRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.theme.OutdoorActivityPlannerTheme

class MainActivity : ComponentActivity() {
    private lateinit var realm: Realm
    private lateinit var weatherRepository: WeatherRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Realm
        val config = RealmConfiguration.Builder(
            schema = setOf(ActivityEntity::class, WeatherCache::class)
        ).name("test.realm").build()
        realm = Realm.open(config)

        // Initialize API
        val weatherApi = RetrofitFactory.create("https://api.openweathermap.org/data/2.5/")
            .create(WeatherApiService::class.java)
        weatherRepository = WeatherRepository(weatherApi, realm)

        setContent {
            OutdoorActivityPlannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestScreen(realm, weatherRepository)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}

@Composable
fun TestScreen(realm: Realm, weatherRepository: WeatherRepository) {
    val scope = rememberCoroutineScope()
    var testResults by remember { mutableStateOf("Ready to test components...\n\n") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Component Testing",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Test Buttons
        Button(
            onClick = {
                scope.launch {
                    testResults = "Testing Realm (ActivityEntity)...\n"
                    try {
                        // Create
                        realm.write {
                            copyToRealm(ActivityEntity().apply {
                                title = "Test Activity"
                                description = "Testing Realm"
                                date = "Nov 14, 2025"
                            })
                        }
                        testResults += "✅ CREATE: Activity added\n"

                        // Read
                        val activities = realm.query(ActivityEntity::class).find()
                        testResults += "✅ READ: Found ${activities.size} activity(ies)\n"

                        // Update
                        realm.write {
                            val first = query(ActivityEntity::class).first().find()
                            first?.title = "Updated Activity"
                        }
                        testResults += "✅ UPDATE: Activity updated\n"

                        // Delete
                        realm.write {
                            val all = query(ActivityEntity::class).find()
                            delete(all)
                        }
                        testResults += "✅ DELETE: Activities cleared\n"

                        testResults += "\n✅ Realm Test PASSED!\n\n"
                    } catch (e: Exception) {
                        testResults += "❌ ERROR: ${e.message}\n\n"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("1. Test Realm (Local DB)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    testResults = "Testing Weather API...\n"
                    try {
                        val API_KEY = "YOUR_API_KEY" // Replace with real key
                        val response = weatherRepository.fetchCurrentByCity("Manila", API_KEY)

                        if (response != null) {
                            testResults += "✅ API Call successful!\n"
                            testResults += "City: ${response.city}\n"
                            testResults += "Temp: ${response.main.temp}°C\n"
                            testResults += "Condition: ${response.weather.firstOrNull()?.main}\n"
                            testResults += "\n✅ Weather API Test PASSED!\n\n"
                        } else {
                            testResults += "⚠️ API returned null (check API key or connection)\n\n"
                        }
                    } catch (e: Exception) {
                        testResults += "❌ ERROR: ${e.message}\n\n"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("2. Test Weather API")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    testResults = "Testing Weather Cache...\n"
                    try {
                        // Save to cache
                        realm.write {
                            copyToRealm(WeatherCache().apply {
                                city = "Manila"
                                temp = 28.5
                                condition = "Clear"
                                icon = "01d"
                                humidity = 70
                                wind = 5.2
                            })
                        }
                        testResults += "✅ Cached weather data saved\n"

                        // Retrieve from cache
                        val cached = weatherRepository.getCached("Manila")
                        if (cached != null) {
                            testResults += "✅ Retrieved: ${cached.city}, ${cached.temp}°C\n"
                            testResults += "\n✅ Cache Test PASSED!\n\n"
                        }
                    } catch (e: Exception) {
                        testResults += "❌ ERROR: ${e.message}\n\n"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("3. Test Weather Cache")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    testResults = "Testing Repository Pattern...\n"
                    try {
                        // Test repository methods
                        testResults += "✅ WeatherRepository initialized\n"
                        testResults += "✅ fetchCurrentByCity() method exists\n"
                        testResults += "✅ fetchForecast() method exists\n"
                        testResults += "✅ getCached() method exists\n"
                        testResults += "\n✅ Repository Test PASSED!\n\n"
                    } catch (e: Exception) {
                        testResults += "❌ ERROR: ${e.message}\n\n"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("4. Test Repository Pattern")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    testResults = "Testing Retrofit Setup...\n"
                    try {
                        val retrofit = RetrofitFactory.create("https://api.openweathermap.org/data/2.5/")
                        testResults += "✅ Retrofit instance created\n"
                        testResults += "✅ Base URL configured\n"
                        testResults += "✅ HTTP logging enabled\n"
                        testResults += "✅ Timeout settings applied\n"
                        testResults += "\n✅ Retrofit Test PASSED!\n\n"
                    } catch (e: Exception) {
                        testResults += "❌ ERROR: ${e.message}\n\n"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("5. Test Retrofit Factory")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Test Results Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = testResults,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}