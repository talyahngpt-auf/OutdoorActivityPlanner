package ph.edu.auf.thalia.hingpit.outdooractivityplanner.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val hasPassword = authViewModel.hasPassword()
    val isGoogleLinked = authViewModel.isGoogleLinked()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showAddPasswordDialog by remember { mutableStateOf(false) }
    var showLinkGoogleDialog by remember { mutableStateOf(false) }
    var showUnlinkGoogleDialog by remember { mutableStateOf(false) }

    val successMessage by authViewModel.successMessage.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    // Google Sign-In launcher for linking
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                authViewModel.linkGoogleAccount(account)
            } catch (e: ApiException) {
                // Handle error
            }
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            kotlinx.coroutines.delay(3000)
            authViewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
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
            // Success Message
            successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(message)
                    }
                }
            }

            // Error Message
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(message)
                    }
                }
            }

            // Password Section
            Text(
                text = "Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    if (hasPassword) {
                        ListItem(
                            headlineContent = { Text("Change Password") },
                            supportingContent = { Text("Update your current password") },
                            leadingContent = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingContent = {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                showChangePasswordDialog = true
                            }
                        )
                    } else {
                        ListItem(
                            headlineContent = { Text("Add Password") },
                            supportingContent = { Text("Set a password for your account") },
                            leadingContent = {
                                Icon(Icons.Default.Add, contentDescription = null)
                            },
                            trailingContent = {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                showAddPasswordDialog = true
                            }
                        )
                    }
                }
            }

            // Linked Accounts Section
            Text(
                text = "Linked Accounts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text("Google Account") },
                        supportingContent = {
                            Text(if (isGoogleLinked) "Connected" else "Not connected")
                        },
                        leadingContent = {
                            Text("🔵", style = MaterialTheme.typography.titleLarge)
                        },
                        trailingContent = {
                            if (isGoogleLinked) {
                                Button(
                                    onClick = { showUnlinkGoogleDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Unlink")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val signInIntent = authViewModel.authManager.getGoogleSignInIntent()
                                        googleSignInLauncher.launch(signInIntent)
                                    }
                                ) {
                                    Text("Link")
                                }
                            }
                        }
                    )
                }
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Account Security",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Link multiple sign-in methods for better security\n" +
                                "• Add a password even if you use Google sign-in\n" +
                                "• You need at least one sign-in method",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // Dialogs
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { current, new ->
                authViewModel.changePassword(current, new)
                showChangePasswordDialog = false
            },
            isLoading = isLoading
        )
    }

    if (showAddPasswordDialog) {
        AddPasswordDialog(
            onDismiss = { showAddPasswordDialog = false },
            onConfirm = { password ->
                authViewModel.addPassword(password)
                showAddPasswordDialog = false
            },
            isLoading = isLoading
        )
    }

    if (showUnlinkGoogleDialog) {
        AlertDialog(
            onDismissRequest = { showUnlinkGoogleDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Unlink Google Account?") },
            text = {
                Text("Make sure you have another sign-in method before unlinking Google.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.unlinkGoogleAccount()
                        showUnlinkGoogleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Unlink")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkGoogleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    isLoading: Boolean
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    supportingText = {
                        if (newPassword.isNotEmpty() && newPassword.length < 6) {
                            Text("Password must be at least 6 characters")
                        }
                    },
                    isError = newPassword.isNotEmpty() && newPassword.length < 6
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Text("Passwords do not match")
                        }
                    },
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(currentPassword, newPassword) },
                enabled = currentPassword.isNotBlank() &&
                        newPassword.length >= 6 &&
                        newPassword == confirmPassword &&
                        !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Change")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Add a password to enable email/password sign-in",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    supportingText = {
                        if (password.isNotEmpty() && password.length < 6) {
                            Text("Password must be at least 6 characters")
                        }
                    },
                    isError = password.isNotEmpty() && password.length < 6
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                            Text("Passwords do not match")
                        }
                    },
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                enabled = password.length >= 6 &&
                        password == confirmPassword &&
                        !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}