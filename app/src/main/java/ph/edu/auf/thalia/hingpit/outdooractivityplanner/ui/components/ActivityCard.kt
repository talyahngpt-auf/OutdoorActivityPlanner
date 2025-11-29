package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils.ActivitySuggestion

@Composable
fun ActivityCard(
    activity: ActivitySuggestion,
    onAddToPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = activity.icon,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(end = 16.dp)
            )

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category chip
                Surface(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = activity.category,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Add button
            FilledTonalButton(
                onClick = onAddToPlan,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add")
            }
        }
    }
}