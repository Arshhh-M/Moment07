package com.example.ui.auth

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.SocialRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class Authenticated(val profile: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: SocialRepository,
    private val context: Context? = null,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
        private const val PREFS_NAME = "socialx_auth_session"
        private const val KEY_USER_ID = "saved_user_id"
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _isCheckingUsername = MutableStateFlow(false)
    val isCheckingUsername: StateFlow<Boolean> = _isCheckingUsername.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkCurrentAuth()
    }

    private fun checkCurrentAuth() {
        viewModelScope.launch {
            // First check Firebase Auth
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val profile = repository.getUserProfile(firebaseUser.uid)
                if (profile != null && profile.username.isNotBlank()) {
                    saveSessionUserId(profile.id)
                    repository.updatePresence(profile.id, true)
                    _uiState.value = AuthUiState.Authenticated(profile)
                    return@launch
                }
            }

            // Check persistent session stored in preferences
            val savedUserId = getSessionUserId()
            if (!savedUserId.isNullOrBlank()) {
                val profile = repository.getUserProfile(savedUserId)
                if (profile != null && profile.username.isNotBlank()) {
                    repository.updatePresence(profile.id, true)
                    _uiState.value = AuthUiState.Authenticated(profile)
                    return@launch
                }
            }

            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    /**
     * Authenticates with Username and Password only.
     * Email is not needed for login as requested.
     */
    fun login(usernameInput: String, passwordInput: String) {
        val cleanUsername = usernameInput.trim().lowercase().removePrefix("@")
        val cleanPassword = passwordInput.trim()

        if (cleanUsername.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your username.")
            return
        }
        if (cleanPassword.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your password.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = AuthUiState.Loading

            try {
                // Find user in Firestore registry by username
                val profile = repository.getUserByUsername(cleanUsername)
                if (profile == null) {
                    _uiState.value = AuthUiState.Error("No account found for @$cleanUsername. Please check your username or create an account.")
                    _isLoading.value = false
                    return@launch
                }

                // Verify password
                val passwordValid = if (profile.passwordHash.isNotBlank()) {
                    val computedHash = hashPassword(cleanPassword, profile.passwordSalt)
                    computedHash == profile.passwordHash
                } else {
                    // Fallback verification if registered through external credential
                    true
                }

                if (!passwordValid) {
                    _uiState.value = AuthUiState.Error("Incorrect password for @$cleanUsername. Please try again.")
                    _isLoading.value = false
                    return@launch
                }

                // Try signing in via Firebase Auth if email is associated (synchronizes auth state)
                if (profile.email.isNotBlank()) {
                    try {
                        auth.signInWithEmailAndPassword(profile.email, cleanPassword).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "FirebaseAuth sign in skipped: ${e.message}")
                    }
                }

                saveSessionUserId(profile.id)
                repository.updatePresence(profile.id, true)
                _uiState.value = AuthUiState.Authenticated(profile)
            } catch (e: Exception) {
                Log.e(TAG, "Login error", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Failed to log in. Please try again.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Creates an account with Email, Username, Display Name, and Password.
     * Email is only used here during registration.
     */
    fun createAccount(
        emailInput: String,
        usernameInput: String,
        displayNameInput: String,
        passwordInput: String,
        photoUrl: String = ""
    ) {
        val cleanEmail = emailInput.trim().lowercase()
        val cleanUsername = usernameInput.trim().lowercase().removePrefix("@")
        val cleanPassword = passwordInput.trim()
        val cleanName = displayNameInput.trim().ifBlank { cleanUsername }

        if (cleanEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address.")
            return
        }

        if (cleanUsername.length < 3) {
            _uiState.value = AuthUiState.Error("Username must be at least 3 characters long.")
            return
        }

        val validUsernameRegex = Regex("^[a-zA-Z0-9_.]+$")
        if (!validUsernameRegex.matches(cleanUsername)) {
            _uiState.value = AuthUiState.Error("Username can only contain letters, numbers, underscores, and periods.")
            return
        }

        if (cleanPassword.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters long.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = AuthUiState.Loading

            try {
                // Check username availability
                val isAvailable = repository.isUsernameAvailable(cleanUsername)
                if (!isAvailable) {
                    _uiState.value = AuthUiState.Error("Username @$cleanUsername is already taken. Please choose another.")
                    _isLoading.value = false
                    return@launch
                }

                // Generate cryptographic salt and hash
                val salt = UUID.randomUUID().toString().replace("-", "").take(16)
                val hash = hashPassword(cleanPassword, salt)
                val newUserId = "user_" + UUID.randomUUID().toString().replace("-", "").take(18)

                val defaultPhoto = photoUrl.ifBlank {
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop"
                }

                val newProfile = UserProfile(
                    id = newUserId,
                    username = cleanUsername,
                    displayName = cleanName,
                    email = cleanEmail,
                    passwordHash = hash,
                    passwordSalt = salt,
                    photoUrl = defaultPhoto,
                    followersCount = 0L,
                    followingCount = 0L,
                    postsCount = 0L,
                    reelsCount = 0L,
                    isOnline = true
                )

                // Save profile & username index in Firestore
                val saved = repository.saveUserProfile(newProfile)
                if (!saved) {
                    _uiState.value = AuthUiState.Error("Failed to save profile. Please try again.")
                    _isLoading.value = false
                    return@launch
                }

                // Attempt syncing with Firebase Auth (graceful fallback if email provider is disabled)
                try {
                    auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword).await()
                } catch (e: Exception) {
                    Log.w(TAG, "FirebaseAuth create user notice: ${e.message}")
                }

                saveSessionUserId(newProfile.id)
                repository.updatePresence(newProfile.id, true)
                _uiState.value = AuthUiState.Authenticated(newProfile)
            } catch (e: Exception) {
                Log.e(TAG, "Registration error", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Failed to create account. Please try again.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((salt + password).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun saveSessionUserId(userId: String) {
        try {
            val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs?.edit()?.putString(KEY_USER_ID, userId)?.apply()
        } catch (e: Exception) {
            Log.w(TAG, "Could not save session to preferences", e)
        }
    }

    private fun getSessionUserId(): String? {
        return try {
            val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs?.getString(KEY_USER_ID, null)
        } catch (e: Exception) {
            null
        }
    }

    fun setError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }

    fun resetError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun signOut() {
        val currentUserId = getSessionUserId() ?: auth.currentUser?.uid
        if (currentUserId != null) {
            viewModelScope.launch {
                repository.updatePresence(currentUserId, false)
            }
        }
        try {
            context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
        } catch (e: Exception) {
            Log.w(TAG, "Could not clear session preferences", e)
        }
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Auth sign out error", e)
        }
        _uiState.value = AuthUiState.Unauthenticated
    }
}
