package com.example.ui.auth

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val usernameError by viewModel.usernameError.collectAsState()
    val isChecking by viewModel.isCheckingUsername.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is AuthUiState.NeedsProfileSetup -> {
                ProfileSetupDialog(
                    defaultName = state.defaultName,
                    defaultPhoto = state.defaultPhoto,
                    usernameError = usernameError,
                    isChecking = isChecking,
                    onSave = { username, name, bio, photo ->
                        viewModel.completeProfileSetup(username, name, bio, photo)
                    }
                )
            }

            else -> {
                val errorMessage = (state as? AuthUiState.Error)?.message
                val isLoading = state is AuthUiState.Loading

                AuthCardContent(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onDismissError = { viewModel.resetError() },
                    onGoogleSignIn = { idToken ->
                        viewModel.signInWithGoogle(idToken)
                    },
                    onError = { message ->
                        viewModel.setError(message)
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthCardContent(
    isLoading: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    onGoogleSignIn: (String) -> Unit,
    onError: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .widthIn(max = 460.dp)
            .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Logo Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryViolet, SecondaryPink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "X",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SocialX",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Share moments, reels, and stories in real time",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Display Banner
            AnimatedVisibility(
                visible = !errorMessage.isNullOrBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Highlights
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureHighlightRow(
                        emoji = "✨",
                        title = "One-Tap Sign In",
                        subtitle = "Instant, secure login with Google"
                    )
                    FeatureHighlightRow(
                        emoji = "🎥",
                        title = "Trending Reels & Stories",
                        subtitle = "Express yourself with full HD videos & posts"
                    )
                    FeatureHighlightRow(
                        emoji = "💬",
                        title = "Real-Time Messaging",
                        subtitle = "Connect and chat with friends anytime"
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Google Sign-In Button (Streamlined Onboarding)
            GoogleSignInButton(
                isLoading = isLoading,
                onSuccess = onGoogleSignIn,
                onError = onError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "By continuing, you agree to SocialX's Terms of Service and Privacy Policy.",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    emoji: String,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Composable
fun ProfileSetupDialog(
    defaultName: String,
    defaultPhoto: String,
    usernameError: String?,
    isChecking: Boolean,
    onSave: (username: String, displayName: String, bio: String, photoUrl: String) -> Unit
) {
    val initialUsername = remember(defaultName) {
        val base = defaultName.trim().lowercase()
            .replace("\\s+".toRegex(), "_")
            .filter { it.isLetterOrDigit() || it == '_' }
        if (base.length >= 2) base.take(20) else ""
    }
    var username by remember { mutableStateOf(initialUsername) }
    var displayName by remember { mutableStateOf(defaultName) }
    var bio by remember { mutableStateOf("Ready to share moments on SocialX! ✨") }
    var selectedPhoto by remember {
        mutableStateOf(
            if (defaultPhoto.isNotBlank()) defaultPhoto
            else "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop"
        )
    }

    val sampleAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&h=200&fit=crop"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .widthIn(max = 480.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to SocialX!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Claim your unique username and finish profile setup.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current Avatar preview
            AsyncImage(
                model = selectedPhoto,
                contentDescription = "Selected Avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose an avatar:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(sampleAvatars) { url ->
                    val isSelected = selectedPhoto == url
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { selectedPhoto = url }
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Avatar option",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' } },
                label = { Text("Unique Username") },
                placeholder = { Text("e.g. alex_rivera") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = "Username symbol")
                },
                isError = usernameError != null,
                supportingText = {
                    if (usernameError != null) {
                        Text(text = usernameError, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(text = "Letters, numbers, and underscores only")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_username_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Display Name
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Display Name")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_display_name_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bio
            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 150) bio = it },
                label = { Text("Bio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_bio_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSave(username, displayName, bio, selectedPhoto) },
                enabled = username.trim().length >= 2 && !isChecking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verifying...")
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join SocialX", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GoogleSignInButton(
    isLoading: Boolean,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSigningInWithGoogle by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = {
            if (isLoading || isSigningInWithGoogle) return@OutlinedButton
            isSigningInWithGoogle = true
            coroutineScope.launch {
                try {
                    val serverClientId = try {
                        context.getString(R.string.default_web_client_id)
                    } catch (e: Exception) {
                        "610538848680-2eh3v0p0kjli6ds5haaj19gmo21l74pj.apps.googleusercontent.com"
                    }
                    val credentialManager = CredentialManager.create(context)
                    val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                        .build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(
                        request = request,
                        context = context
                    )
                    val credential = result.credential
                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        onSuccess(googleIdTokenCredential.idToken)
                    } else {
                        onError("Unexpected credential format returned from Google Auth")
                    }
                } catch (e: GetCredentialCancellationException) {
                    // User cancelled the prompt dialog; no error banner needed
                } catch (e: Exception) {
                    Log.e("AuthScreen", "Google Sign-In failed", e)
                    onError("Google Sign-In failed: ${e.localizedMessage ?: e.message}")
                } finally {
                    isSigningInWithGoogle = false
                }
            }
        },
        enabled = !isLoading && !isSigningInWithGoogle,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("google_sign_in_button"),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        if (isSigningInWithGoogle || (isLoading && isSigningInWithGoogle)) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Connecting with Google...",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        } else {
            GoogleLogoIcon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val strokeW = w * 0.18f

        // Blue right arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(strokeW)
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(strokeW)
        )
        // Yellow bottom-left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(strokeW)
        )
        // Red top-left arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(strokeW)
        )
        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(w * 0.45f, h * 0.5f),
            end = Offset(w * 0.95f, h * 0.5f),
            strokeWidth = strokeW
        )
    }
}
