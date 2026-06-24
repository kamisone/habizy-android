package com.habizy.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.habizy.app.ui.components.AppSnackbarHost
import com.habizy.app.ui.components.BottomNavBar
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.screens.AddReceiptScreen
import com.habizy.app.ui.screens.AdminSettingsScreen
import com.habizy.app.ui.screens.ArticleStatsScreen
import com.habizy.app.ui.screens.CompleteProfileScreen
import com.habizy.app.ui.screens.ContributeScreen
import com.habizy.app.ui.screens.CreateReportScreen
import com.habizy.app.ui.screens.CreateUserScreen
import com.habizy.app.ui.screens.DecideForMeScreen
import com.habizy.app.ui.screens.ExpensesScreen
import com.habizy.app.ui.screens.HomeScreen
import com.habizy.app.ui.screens.LoginScreen
import com.habizy.app.ui.screens.MembersScreen
import com.habizy.app.ui.screens.MenageScreen
import com.habizy.app.ui.screens.NotificationsScreen
import com.habizy.app.ui.screens.ProfileScreen
import com.habizy.app.ui.screens.PurchaseHistoryScreen
import com.habizy.app.ui.screens.ReportDetailScreen
import com.habizy.app.ui.screens.ReportsScreen
import com.habizy.app.ui.screens.RotationScreen
import com.habizy.app.ui.screens.ShoppingListScreen
import com.habizy.app.ui.screens.StatsScreen
import com.habizy.app.ui.theme.ScreenBackground

@Composable
fun AppNavigation(
    isLoggedIn: Boolean,
    profileCompleted: Boolean,
    onLogout: () -> Unit,
    deepLinkRoute: String? = null,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalSnackbarHost provides snackbarHostState) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                !isLoggedIn -> AuthNavigation()
                !profileCompleted -> CompleteProfileScreen()
                else -> MainNavigation(onLogout = onLogout, deepLinkRoute = deepLinkRoute)
            }

            AppSnackbarHost(hostState = snackbarHostState)
        }
    }
}

// ---- Auth flow ----

@Composable
private fun AuthNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable(Screen.Login.route) {
            LoginScreen()
        }
    }
}

// ---- Main app with tabs ----

@Composable
private fun MainNavigation(
    onLogout: () -> Unit,
    deepLinkRoute: String? = null,
) {
    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabRoutes = Screen.tabRoutes

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route
        val tabIndex = tabRoutes.indexOf(currentRoute)
        if (tabIndex >= 0) {
            selectedTab = tabIndex
        }
    }

    LaunchedEffect(deepLinkRoute) {
        deepLinkRoute?.let { route ->
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    navController.navigate(tabRoutes[index]) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            // -- Tab destinations --
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToRotation = { navController.navigate(Screen.Rotation.route) },
                    onNavigateToShopping = { navController.navigate(Screen.Shopping.route) },
                    onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    onNavigateToHistory = { navController.navigate(Screen.PurchaseHistory.route) },
                    onNavigateToMenage = { navController.navigate(Screen.Menage.route) },
                    onNavigateToReportDetail = { id ->
                        navController.navigate(Screen.ReportDetail.createRoute(id))
                    },
                )
            }
            composable(Screen.Reports.route) {
                ReportsScreen(
                    onNavigateToReportDetail = { id ->
                        navController.navigate(Screen.ReportDetail.createRoute(id))
                    },
                    onNavigateToCreateReport = { navController.navigate(Screen.CreateReport.route) },
                )
            }
            composable(Screen.Menage.route) {
                MenageScreen()
            }
            composable(Screen.Expenses.route) {
                ExpensesScreen(
                    onNavigateToArticleStats = { navController.navigate(Screen.ArticleStats.route) },
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToMembers = { navController.navigate(Screen.Members.route) },
                    onNavigateToCreateUser = { navController.navigate(Screen.CreateUser.route) },
                    onNavigateToAdmin = { navController.navigate(Screen.AdminSettings.route) },
                    onLogout = onLogout,
                )
            }

            // -- Non-tab destinations --
            composable(Screen.Shopping.route) {
                ShoppingListScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.Contribute.route) {
                ContributeScreen()
            }
            composable(Screen.Rotation.route) {
                RotationScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToAddReceipt = { navController.navigate(Screen.AddReceipt.route) },
                )
            }
            composable(Screen.AddReceipt.route) {
                AddReceiptScreen(
                    onBack = { navController.popBackStack() },
                    onReceiptCreated = { navController.popBackStack() },
                )
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.EmptyKitty.route) {
                PlaceholderScreen("Cagnotte vide")
            }
            composable(Screen.CreateUser.route) {
                CreateUserScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.Members.route) {
                MembersScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.AdminSettings.route) {
                AdminSettingsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.DecideForMe.route) {
                DecideForMeScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.PurchaseHistory.route) {
                PurchaseHistoryScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.CompleteProfile.route) {
                CompleteProfileScreen()
            }
            composable(Screen.ArticleStats.route) {
                ArticleStatsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.CreateReport.route) {
                CreateReportScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack() },
                )
            }
            composable(
                route = Screen.ReportDetail.route,
                arguments = listOf(navArgument("reportId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
                ReportDetailScreen(
                    reportId = reportId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = name)
    }
}
