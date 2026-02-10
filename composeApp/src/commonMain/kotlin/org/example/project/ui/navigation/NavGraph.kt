package org.example.project.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
    data object Home : Screen("home_screen", "Inicio", Icons.Default.Home)
    data object Projects : Screen("projects", "Proyectos", Icons.Default.Folder)
    data object Tasks : Screen("tasks_screen/{sectionId}", "Agenda", Icons.Default.DateRange) {
        // Función de ayuda para construir la ruta: "tasks_screen/0" o "tasks_screen/4"
        fun createRoute(sectionId: Long) = "tasks_screen/$sectionId"
    }
    data object Agenda : Screen("agenda_screen", "Agenda", Icons.Default.DateRange)
    data object Profile : Screen("profile", "Perfil", Icons.Default.Person)


}