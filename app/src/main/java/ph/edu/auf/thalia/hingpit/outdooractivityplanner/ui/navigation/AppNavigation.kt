package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.HomeScreen
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.ActivityPlannerScreen
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.ForecastScreen
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

//        composable("home") {
//            HomeScreen(navController)
//        }

        composable("activities") {
            ActivityPlannerScreen(navController)
        }
//
//        composable("forecast") {
//            ForecastScreen(navController)
//        }

        composable("settings") {
            SettingsScreen(navController)
        }
    }
}