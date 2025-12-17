package ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.auth.FirebaseAuthManager
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository.UserPreferencesRepository
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.sync.FirebaseSyncManager


class AuthViewModel(
    val authManager: FirebaseAuthManager,
    private val syncManager: FirebaseSyncManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {


    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()


    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()


    val currentUser: FirebaseUser?
        get() = authManager.currentUser


    val currentUserId: String?
        get() = authManager.currentUserId


    val isUserSignedIn: Boolean
        get() = authManager.isUserSignedIn


    init {
        observeAuthState()
    }


    private fun observeAuthState() {
        viewModelScope.launch {
            authManager.authStateFlow.collect { user ->
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }


    // EMAIL/PASSWORD AUTHENTICATION


    fun signUpWithEmail(email: String, password: String, displayName: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null


                val result = authManager.signUpWithEmail(email, password, displayName)


                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let {
                        userPreferencesRepository.createDefaultPreferences(it.uid)
                        _authState.value = AuthState.Authenticated(it)
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Sign up failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null


                val result = authManager.signInWithEmail(email, password)


                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let {
                        _authState.value = AuthState.Authenticated(it)


                        val preferences = userPreferencesRepository.getPreferences(it.uid)
                        if (preferences == null) {
                            userPreferencesRepository.createDefaultPreferences(it.uid)
                        }
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Sign in failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun signInAnonymously() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null


                val result = authManager.signInAnonymously()


                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let {
                        userPreferencesRepository.createDefaultPreferences(it.uid)
                        _authState.value = AuthState.Authenticated(it)
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Anonymous sign in failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // GOOGLE SIGN-IN


    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null


                val result = authManager.signInWithGoogle(account)


                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let {
                        _authState.value = AuthState.Authenticated(it)


                        val preferences = userPreferencesRepository.getPreferences(it.uid)
                        if (preferences == null) {
                            userPreferencesRepository.createDefaultPreferences(it.uid)
                        }
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Google sign in failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun linkGoogleAccount(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null


                val result = authManager.linkGoogleAccount(account)


                if (result.isSuccess) {
                    _successMessage.value = "Google account linked successfully"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to link Google account"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun unlinkGoogleAccount() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null


                val result = authManager.unlinkGoogleAccount()


                if (result.isSuccess) {
                    _successMessage.value = "Google account unlinked successfully"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to unlink Google account"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun isGoogleLinked(): Boolean = authManager.isGoogleLinked()


    // PASSWORD MANAGEMENT


    fun hasPassword(): Boolean = authManager.hasPassword()


    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null


                val result = authManager.changePassword(currentPassword, newPassword)


                if (result.isSuccess) {
                    _successMessage.value = "Password changed successfully"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to change password"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun addPassword(password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null


                val result = authManager.addPassword(password)


                if (result.isSuccess) {
                    _successMessage.value = "Password added successfully"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to add password"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null


                val result = authManager.sendPasswordResetEmail(email)


                if (result.isSuccess) {
                    _successMessage.value = "Password reset email sent"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send reset email"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ACCOUNT MANAGEMENT


    fun signOut() {
        viewModelScope.launch {
            try {
                authManager.signOut()
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign out failed"
            }
        }
    }


    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null


                val userId = currentUserId
                if (userId != null) {
                    syncManager.deleteAllUserData(userId)
                    userPreferencesRepository.deletePreferences(userId)


                    val result = authManager.deleteAccount()


                    if (result.isSuccess) {
                        _authState.value = AuthState.Unauthenticated
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to delete account"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun clearError() {
        _errorMessage.value = null
    }


    fun clearSuccess() {
        _successMessage.value = null
    }
}


sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
}
