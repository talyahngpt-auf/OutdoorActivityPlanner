package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.AuthState
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.AuthViewModel
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.WeatherViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    weatherViewModel: WeatherViewModel,
    onSplashComplete: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    // Animation for app logo
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    LaunchedEffect(Unit) {
        // Show splash for 2 seconds
        delay(2000)

        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🌤️",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.scale(scale * 1.5f)
            )

            // App Name
            Text(
                text = "W.A.R.M",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Text(
                text = "Weather-Aware Routine Mapper",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading indicator
            Text(
                text = "Initializing...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        }
    }
}