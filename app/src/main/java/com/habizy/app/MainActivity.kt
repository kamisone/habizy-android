package com.habizy.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habizy.app.ui.navigation.AppNavigation
import com.habizy.app.ui.theme.CagnotteTheme
import com.habizy.app.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private var deepLinkRoute by mutableStateOf<String?>(null)
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleDeepLinkIntent(intent)

        setContent {
            CagnotteTheme {
                val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
                val profileCompleted by authViewModel.profileCompleted.collectAsStateWithLifecycle(initialValue = null)

                AppNavigation(
                    isLoggedIn = isLoggedIn,
                    profileCompleted = profileCompleted != false,
                    onLogout = { authViewModel.logout() },
                    deepLinkRoute = deepLinkRoute,
                )
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
