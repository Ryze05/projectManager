package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.repository.AuthRepository
import org.example.project.ui.auth.AuthViewModel
import org.example.project.ui.auth.LoginScreen
import org.example.project.ui.auth.RegisterScreen
import org.example.project.ui.home.HomeScreen
import org.example.project.ui.projects.ProjectsScreen // Importar pantalla Proyectos
import org.example.project.ui.profile.ProfileScreen // Importar pantalla Perfil
import org.example.project.ui.navigation.Screen
import org.example.project.ui.theme.ProjectManagerTheme

@Composable
fun App() {
    ProjectManagerTheme {
        val navController = rememberNavController()
        val authRepository = remember { AuthRepository() }
        val viewModelAuth = remember { AuthViewModel(authRepository) }

        // Obtenemos la ruta actual para saber si mostrar el BottomBar
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Lista de pantallas que SÍ tienen barra inferior
        val bottomBarScreens = listOf(
            Screen.Home,
            Screen.Projects,
            Screen.Tasks,
            Screen.Profile
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                // SOLO mostramos la barra si la ruta actual está en nuestra lista
                if (bottomBarScreens.any { it.route == currentRoute }) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        bottomBarScreens.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        // Usamos la ruta de Screen.Home directamente
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            // NavHost recibe el padding del Scaffold para que el contenido no quede tapado por la barra
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                // --- GRAFO DE AUTENTICACIÓN ---
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = viewModelAuth,
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = viewModelAuth,
                        onBackToLogin = { navController.popBackStack() }
                    )
                }

                // --- PANTALLAS PRINCIPALES (CON TABS) ---
                composable(Screen.Home.route) {
                    HomeScreen() // Tu pantalla Home existente
                }

                composable(Screen.Projects.route) {
                    ProjectsScreen() // La nueva pantalla de proyectos
                }

                composable(Screen.Tasks.route) {
                    // Placeholder para la Agenda/Tareas (aún no la hemos hecho)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Pantalla de Agenda/Tareas")
                    }
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            // Lógica de logout
                            viewModelAuth.logout() // Asumiendo que añades esto al VM
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) // Limpia toda la pila
                            }
                        }
                    )
                }
            }
        }
    }
}