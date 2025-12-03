package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == "today",
            onClick = {
                if (currentRoute != "today") {
                    navController.navigate("today")
                }
            },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Today") },
            label = { Text("Today") }
        )

        NavigationBarItem(
            selected = currentRoute == "activities",
            onClick = {
                if (currentRoute != "activities") {
                    navController.navigate("activities")
                }
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Activity Log") },
            label = { Text("Log") }
        )

        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = {
                if (currentRoute != "settings") {
                    navController.navigate("settings")
                }
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}