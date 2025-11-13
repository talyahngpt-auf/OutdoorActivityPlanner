package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*

@Composable
fun OutdoorPlannerApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable("home") { Text("🏠 Home – Weather Overview") }
            composable("suggested") { Text("🎯 Suggested Activities") }
            composable("log") { Text("📅 Activity Log") }
            composable("settings") { Text("⚙️ Settings") }
        }
    }
}

@Composable
fun BottomNavBar(navController: androidx.navigation.NavHostController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home") },
            label = { Text("Home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("suggested") },
            label = { Text("Suggested") },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Suggested") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("log") },
            label = { Text("Log") },
            icon = { Icon(Icons.Default.List, contentDescription = "Log") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("settings") },
            label = { Text("Settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") }
        )
    }
}
