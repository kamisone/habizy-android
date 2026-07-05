package com.habizy.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habizy.app.ui.navigation.AppNavigation
import com.habizy.app.ui.screens.AppSplashScreen
import com.habizy.app.ui.theme.HabizyTheme
import com.habizy.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var deepLinkRoute by mutableStateOf<String?>(null)
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Dismiss the OS splash as soon as the first Compose frame is drawn.
        // The full-screen AppSplashScreen composable below takes over immediately.
        splashScreen.setKeepOnScreenCondition { false }

        handleDeepLinkIntent(intent)

        setContent {
            HabizyTheme {
                val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
                val profileCompleted by authViewModel.profileCompleted.collectAsStateWithLifecycle(initialValue = null)
                var showSplash by remember { mutableStateOf(true) }

                Box(Modifier.fillMaxSize()) {
                    AppNavigation(
                        isLoggedIn = isLoggedIn,
                        profileCompleted = profileCompleted != false,
                        onLogout = { authViewModel.logout() },
                        deepLinkRoute = deepLinkRoute,
                    )

                    AnimatedVisibility(
                        visible = showSplash,
                        exit = fadeOut(animationSpec = tween(durationMillis = 400)),
                    ) {
                        AppSplashScreen()
                    }
                }

                LaunchedEffect(Unit) {
                    delay(1500)
                    showSplash = false
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val target = intent?.getStringExtra("navigate_to") ?: return
        deepLinkRoute = target
    }
}
