package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.KosRepository
import com.example.ui.KosConnectApp
import com.example.ui.KosViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.KosConnectTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Step 1: Configure App Check with launch intent before any UI or Firebase operations
        com.example.util.configureAppCheck(this, intent)
        // Step 2: Initialize notification channels and periodic WorkManager task (Opsi B)
        com.example.reminder.NotificationHelper.createNotificationChannels(this)
        com.example.reminder.DueReminderWorker.enqueuePeriodicReminder(this)
        enableEdgeToEdge()
        setContent {
            KosConnectTheme {
                AppRootContent()
            }
        }
    }
}

@Composable
fun AppRootContent() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    if (currentUser == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        val user = currentUser!!
        val kosViewModel: KosViewModel = viewModel(
            key = user.uid,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                    KosViewModel(KosRepository(app))
                }
            }
        )
        KosConnectApp(viewModel = kosViewModel, user = user)
    }
}
