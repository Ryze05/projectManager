package org.example.project.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // Auth
    data object Login : Screen("login", "Login", Icons.Default.Home)
    data object Register : Screen("register", "Registro", Icons.Default.Home)

    // Tabs Principales
    data object Home : Screen("home", "Dashboard", Icons.Default.Home)
    data object Projects : Screen("projects", "Proyectos", Icons.Default.Folder)
    data object Tasks : Screen("tasks", "Agenda", Icons.Default.DateRange)
    data object Profile : Screen("profile", "Perfil", Icons.Default.Person)

    data object Chat : Screen("chat_screen/{projectId}", "Chat", Icons.Default.Comment) {
        fun createRoute(projectId: Long) = "chat_screen/$projectId"
    }
}