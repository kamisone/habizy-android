package com.habizy.app.ui.navigation

/**
 * Sealed class defining all screen routes in the app, mirroring the iOS navigation structure.
 */
sealed class Screen(val route: String) {

    // Auth
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Main tabs
    data object Home : Screen("home")
    data object Reports : Screen("reports")
    data object Menage : Screen("menage")
    data object Expenses : Screen("expenses")
    data object Profile : Screen("profile")

    // Features
    data object Shopping : Screen("shopping")
    data object Contribute : Screen("contribute")
    data object Rotation : Screen("rotation")
    data object AddReceipt : Screen("add_receipt")
    data object Notifications : Screen("notifications")
    data object EmptyKitty : Screen("empty_kitty")
    data object CreateUser : Screen("create_user")
    data object Members : Screen("members")
    data object AdminSettings : Screen("admin_settings")
    data object DecideForMe : Screen("decide_for_me")
    data object Stats : Screen("stats")
    data object PurchaseHistory : Screen("purchase_history")
    data object CompleteProfile : Screen("complete_profile")
    data object ArticleStats : Screen("article_stats")
    data object CreateReport : Screen("create_report")

    // Parameterized routes
    data object ReportDetail : Screen("report_detail/{reportId}") {
        fun createRoute(reportId: String): String = "report_detail/$reportId"
    }

    companion object {
        /** All tab routes used by the bottom navigation bar. */
        val tabRoutes = listOf(
            Home.route,
            Reports.route,
            Menage.route,
            Expenses.route,
            Profile.route,
        )
    }
}
