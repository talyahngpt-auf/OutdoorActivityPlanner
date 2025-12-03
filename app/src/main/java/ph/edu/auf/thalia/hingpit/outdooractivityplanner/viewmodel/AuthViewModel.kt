package ph.edu.auf.thalia.hingpit.outdooractivityplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun signUpWithEmail(email: String, password: String, displayName: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = authManager.signUpWithEmail(email, password, displayName)

                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let {
                        // Create default preferences for new user
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

                        // Check if preferences exist, if not create default
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
                        // Create default preferences for anonymous user
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

    // ========== EMAIL LINK (PASSWORDLESS) SIGN-IN ==========

    fun sendSignInLinkToEmail(email: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = authManager.sendSignInLinkToEmail(email)

                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send sign-in link"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isSignInLink(link: String): Boolean {
        return authManager.isSignInLink(link)
    }

    fun signInWithEmailLink(email: String, link: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = authManager.signInWithEmailLink(email, link)

                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let {
                        _authState.value = AuthState.Authenticated(it)

                        // Check if preferences exist, if not create default
                        val preferences = userPreferencesRepository.getPreferences(it.uid)
                        if (preferences == null) {
                            userPreferencesRepository.createDefaultPreferences(it.uid)
                        }
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Sign in with link failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSavedEmailForSignIn(): String? {
        return authManager.getSavedEmail()
    }

    // ========== GOOGLE SIGN-IN ==========

    fun signInWithGoogle(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = authManager.signInWithGoogle(account)

                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let {
                        _authState.value = AuthState.Authenticated(it)

                        // Check if preferences exist, if not create default
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

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = authManager.sendPasswordResetEmail(email)

                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send reset email"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
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
                    // Delete cloud data
                    syncManager.deleteAllUserData(userId)

                    // Delete local data
                    userPreferencesRepository.deletePreferences(userId)

                    // Delete account
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
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
}