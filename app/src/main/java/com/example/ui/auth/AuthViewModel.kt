package com.example.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.SocialRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class NeedsProfileSetup(val user: FirebaseUser, val defaultName: String, val defaultPhoto: String) : AuthUiState
    data class Authenticated(val profile: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: SocialRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
        private const val INTERNAL_EMAIL_DOMAIN = "socialx.app"
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _isCheckingUsername = MutableStateFlow(false)
    val isCheckingUsername: StateFlow<Boolean> = _isCheckingUsername.asStateFlow()

    init {
        checkCurrentAuth()
    }

    private fun checkCurrentAuth() {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AuthUiState.Unauthenticated
        } else {
            viewModelScope.launch {
                loadProfileForUser(user)
            }
        }
    }

    private suspend fun loadProfileForUser(user: FirebaseUser) {
        val profile = repository.getUserProfile(user.uid)
        if (profile != null && profile.username.isNotBlank()) {
            _uiState.value = AuthUiState.Authenticated(profile)
            repository.updatePresence(user.uid, true)
        } else {
            _uiState.value = AuthUiState.NeedsProfileSetup(
                user = user,
                defaultName = user.displayName ?: "SocialX User",
                defaultPhoto = user.photoUrl?.toString() ?: ""
            )
        }
    }

    /**
     * Google Sign-In is the configured identity provider on this project.
     */
    fun login(usernameInput: String, passwordInput: String) {
        _uiState.value = AuthUiState.Error("Please sign in using the 'Continue with Google' button.")
    }

    /**
     * Account creation is streamlined via Google Sign-In with 1-tap onboarding.
     */
    fun createAccount(
        usernameInput: String,
        nameInput: String,
        passwordInput: String,
        emailInput: String,
        photoUrl: String
    ) {
        _uiState.value = AuthUiState.Error("Please use the 'Continue with Google' button to create your account.")
    }

    /**
     * Authenticates a user using Google Sign-In with an ID token from Credential Manager.
     * Streamlines onboarding by auto-creating or linking user profiles.
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    loadProfileForUser(user)
                } else {
                    _uiState.value = AuthUiState.Error("Google Sign-In failed: No user information returned.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google sign-in error", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Google Sign-In failed. Please try again.")
            }
        }
    }

    fun completeProfileSetup(
        username: String,
        displayName: String,
        bio: String,
        photoUrl: String
    ) {
        val user = auth.currentUser ?: return
        val cleanUsername = username.trim().lowercase().removePrefix("@")
        if (cleanUsername.length < 2) {
            _usernameError.value = "Username must be at least 2 characters"
            return
        }

        viewModelScope.launch {
            _isCheckingUsername.value = true
            _usernameError.value = null
            val isAvail = repository.isUsernameAvailable(cleanUsername, user.uid)
            if (!isAvail) {
                _usernameError.value = "Username @$cleanUsername is already taken"
                _isCheckingUsername.value = false
                return@launch
            }

            val newProfile = UserProfile(
                id = user.uid,
                username = cleanUsername,
                displayName = displayName.ifBlank { "SocialX User" },
                email = user.email ?: "",
                bio = bio.trim(),
                photoUrl = photoUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop" },
                isOnline = true
            )

            val saved = repository.saveUserProfile(newProfile)
            _isCheckingUsername.value = false
            if (saved) {
                _uiState.value = AuthUiState.Authenticated(newProfile)
                repository.updatePresence(user.uid, true)
            } else {
                _usernameError.value = "Failed to save profile. Please try again."
            }
        }
    }

    fun setError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }

    fun signOut() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                repository.updatePresence(uid, false)
                auth.signOut()
                _uiState.value = AuthUiState.Unauthenticated
            }
        } else {
            auth.signOut()
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun resetError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }
}
