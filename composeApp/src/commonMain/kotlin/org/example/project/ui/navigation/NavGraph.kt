package org.example.project.ui.navigation

sealed class Screen(val route: String) {
    data object Login: Screen("login_screen")
    data object Register : Screen("register_screen")
    data object Home : Screen("home_screen")
}