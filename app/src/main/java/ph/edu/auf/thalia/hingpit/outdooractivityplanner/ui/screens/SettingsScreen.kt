package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.components.BottomNavigationBar
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.AuthState
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.AuthViewModel
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.SyncStatus
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.SyncViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    syncViewModel: SyncViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val isSyncing by syncViewModel.isSyncing.collectAsState()
    val lastSyncTimestamp by syncViewModel.lastSyncTimestamp.collectAsState()
    val syncStatus by syncViewModel.syncStatus.collectAsState()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Section
            when (val state = authState) {
                is AuthState.Authenticated -> {
                    // User Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = state.user.displayName ?: "User",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = state.user.email ?: "No email",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    if (state.user.isAnonymous) {
                                        Text(
                                            text = "Guest Account",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sync Section
                    Text(
                        text = "Data Synchronization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Sync Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Last Sync",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = lastSyncTimestamp?.let {
                                            SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(it))
                                        } ?: "Never synced",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }

                                // Sync Status Indicator
                                when (syncStatus) {
                                    is SyncStatus.Syncing -> {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                    is SyncStatus.Success -> {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Synced",
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    is SyncStatus.Error -> {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = "Error",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    else -> {}
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sync Button
                            Button(
                                onClick = { syncViewModel.performSync() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isSyncing
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isSyncing) "Syncing..." else "Sync Now")
                            }

                            // Sync Info
                            if (syncStatus is SyncStatus.Success) {
                                val result = (syncStatus as SyncStatus.Success).result
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = "✓ ${result.activitiesSynced} activities synced",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = if (result.preferencesSynced) "✓ Preferences synced" else "✗ Preferences not synced",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            if (syncStatus is SyncStatus.Error) {
                                val message = (syncStatus as SyncStatus.Error).message
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "Error: $message",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    // Account Actions
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ListItem(
                                headlineContent = { Text("Sign Out") },
                                leadingContent = {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                                },
                                modifier = Modifier.clickable {
                                    showSignOutDialog = true
                                }
                            )

                            HorizontalDivider()

                            ListItem(
                                headlineContent = {
                                    Text(
                                        "Delete Account",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                modifier = Modifier.clickable {
                                    showDeleteAccountDialog = true
                                }
                            )
                        }
                    }
                }

                is AuthState.Unauthenticated -> {
                    // Not signed in
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👤",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Not Signed In",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sign in to sync your data across devices",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate("login") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sign In")
                            }
                        }
                    }
                }

                else -> {
                    // Loading
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // App Info
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("App Version") },
                        supportingContent = { Text("1.0.0") },
                        leadingContent = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        }
                    )
                }
            }
        }
    }

    // Sign Out Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
            },
            title = { Text("Sign Out?") },
            text = { Text("Are you sure you want to sign out? Your data will remain stored locally.") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.signOut()
                        showSignOutDialog = false
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Account?") },
            text = {
                Text("This action cannot be undone. All your data will be permanently deleted from both local storage and the cloud.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.deleteAccount()
                        showDeleteAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}