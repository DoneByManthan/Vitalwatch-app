package com.vitalwatch.app.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object History : Screen("history")
    object Device : Screen("device")
    object Profile : Screen("profile")
}
