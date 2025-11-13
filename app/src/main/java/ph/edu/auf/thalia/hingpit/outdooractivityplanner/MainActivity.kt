package ph.edu.auf.thalia.hingpit.outdooractivityplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.theme.OutdoorActivityPlannerTheme
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OutdoorActivityPlannerTheme {
                AppNavigation()
            }
        }
    }
}
