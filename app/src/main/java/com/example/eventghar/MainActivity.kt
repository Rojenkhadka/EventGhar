package com.example.eventghar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.eventghar.data.ThemeDataStore
import com.example.eventghar.ui.theme.EventGharTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        // Read persisted theme for the current user before first frame to avoid flicker
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val initialDarkTheme = runBlocking {
            ThemeDataStore.isDarkModeFlow(applicationContext, currentUid).first()
        }

        setContent {
            var isDarkTheme by remember { mutableStateOf(initialDarkTheme) }

            // Re-read theme whenever the Firebase user changes (login / logout / switch account)
            val auth = FirebaseAuth.getInstance()
            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    val uid = firebaseAuth.currentUser?.uid
                    lifecycleScope.launch(Dispatchers.IO) {
                        val savedTheme = ThemeDataStore.isDarkModeFlow(applicationContext, uid).first()
                        // Switch back to main thread to update state
                        launch(Dispatchers.Main) {
                            isDarkTheme = savedTheme
                        }
                    }
                }
                auth.addAuthStateListener(listener)
                onDispose { auth.removeAuthStateListener(listener) }
            }

            EventGharTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = {
                        isDarkTheme = !isDarkTheme
                        val newValue = isDarkTheme
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        lifecycleScope.launch(Dispatchers.IO) {
                            ThemeDataStore.setDarkMode(applicationContext, uid, newValue)
                        }
                    }
                )
            }
        }
    }
}



