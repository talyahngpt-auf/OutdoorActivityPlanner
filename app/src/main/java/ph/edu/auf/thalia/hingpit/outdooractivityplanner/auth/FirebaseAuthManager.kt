package ph.edu.auf.thalia.hingpit.outdooractivityplanner.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Google Sign-In Client
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getWebClientId())
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isUserSignedIn: Boolean
        get() = auth.currentUser != null

    // Auth state as Flow
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    // Get Web Client ID from resources
    private fun getWebClientId(): String {
        return try {
            val resourceId = context.resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName
            )
            context.getString(resourceId)
        } catch (e: Exception) {
            "479490583620-pnsa5d8n3gu5hnp6ld2b087pcl0b07ts.apps.googleusercontent.com"
        }
    }

    // ========== EMAIL/PASSWORD AUTHENTICATION ==========
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String? = null
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            // Update display name if provided
            displayName?.let {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(it)
                    .build()
                result.user?.updateProfile(profileUpdates)?.await()
            }

            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("User creation failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign in with email and password
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Sign in anonymously
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Anonymous sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== GOOGLE SIGN-IN ==========

    // Get Google Sign-In Intent
    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    // Sign in with Google credential
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()

            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Google sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Link Google account to existing user
    suspend fun linkGoogleAccount(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = user.linkWithCredential(credential).await()

            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Failed to link Google account"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Unlink Google account
    suspend fun unlinkGoogleAccount(): Result<FirebaseUser> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            val result = user.unlink(GoogleAuthProvider.PROVIDER_ID).await()

            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Failed to unlink Google account"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check if Google account is linked
    fun isGoogleLinked(): Boolean {
        val user = auth.currentUser ?: return false
        return user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
    }

    // Sign out from Google
    suspend fun signOutGoogle(): Result<Unit> {
        return try {
            googleSignInClient.signOut().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== PASSWORD MANAGEMENT ==========

    // Check if user has password (email/password provider)
    fun hasPassword(): Boolean {
        val user = auth.currentUser ?: return false
        return user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
    }

    // Change password (requires recent authentication)
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            val email = user.email ?: return Result.failure(Exception("No email associated with account"))

            // Re-authenticate with current password
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            // Update password
            user.updatePassword(newPassword).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add password to Google account (link email/password provider)
    suspend fun addPassword(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            val email = user.email ?: return Result.failure(Exception("No email associated with account"))

            // Create email/password credential
            val credential = EmailAuthProvider.getCredential(email, password)

            // Link credential to user
            user.linkWithCredential(credential).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Re-authenticate with Google (needed before sensitive operations)
    suspend fun reauthenticateWithGoogle(account: GoogleSignInAccount): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== USER PROFILE ==========

    // Update user profile
    suspend fun updateUserProfile(displayName: String? = null): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            val profileUpdates = UserProfileChangeRequest.Builder()
                .apply {
                    displayName?.let { setDisplayName(it) }
                }
                .build()
            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user email
    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    // Get user display name
    fun getUserDisplayName(): String? {
        return auth.currentUser?.displayName
    }

    // Check if email is verified
    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }

    // Send email verification
    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get provider info (Email, Google, Anonymous, etc.)
    fun getProviderInfo(): List<String> {
        val user = auth.currentUser ?: return emptyList()
        return user.providerData.map { it.providerId }
    }

    // ========== ACCOUNT MANAGEMENT ==========

    // Sign out
    fun signOut() {
        auth.signOut()
    }

    // Delete user account
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}