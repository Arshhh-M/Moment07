package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.SocialRepository
import com.example.ui.SocialApp
import com.example.ui.SocialViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.SocialXTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SocialXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SocialXRoot()
                }
            }
        }
    }
}

@Composable
fun SocialXRoot() {
    val authViewModel: AuthViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    ?: error("Application missing")
                val databaseId = app.getString(R.string.firestore_database_id)
                val firestore = FirebaseFirestore.getInstance(databaseId)
                AuthViewModel(
                    repository = SocialRepository(firestore),
                    context = app.applicationContext
                )
            }
        }
    )

    val authState by authViewModel.uiState.collectAsState()

    when (val state = authState) {
        is AuthUiState.Authenticated -> {
            val socialViewModel: SocialViewModel = viewModel(
                key = state.profile.id,
                factory = viewModelFactory {
                    initializer {
                        val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                            ?: error("Application missing")
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val firestore = FirebaseFirestore.getInstance(databaseId)
                        SocialViewModel(
                            repository = SocialRepository(firestore),
                            currentUserId = state.profile.id
                        )
                    }
                }
            )

            SocialApp(
                viewModel = socialViewModel,
                authViewModel = authViewModel,
                onSignOut = { authViewModel.signOut() }
            )
        }
        else -> {
            AuthScreen(viewModel = authViewModel)
        }
    }
}
