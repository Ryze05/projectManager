package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.repository.AuthRepository
import org.example.project.ui.auth.AuthViewModel
import org.example.project.ui.auth.LoginScreen
import org.example.project.ui.auth.RegisterScreen
import org.example.project.ui.home.HomeScreen
import org.example.project.ui.projects.ProjectsScreen
import org.example.project.ui.profile.ProfileScreen
import org.example.project.ui.navigation.Screen
import org.example.project.ui.theme.ProjectManagerTheme

@Composable
fun App() {
    ProjectManagerTheme {
        val navController = rememberNavController()
        val authRepository = remember { AuthRepository() }
        val viewModelAuth = remember { AuthViewModel(authRepository) }

        // --- 1. LÓGICA DE SESIÓN PERSISTENTE ---
        var isLoadingSession by remember { mutableStateOf(true) }
        var startDestination by remember { mutableStateOf(Screen.Login.route) }

        LaunchedEffect(Unit) {
            val session = authRepository.isUserLoggedIn()
            if (session) {
                startDestination = Screen.Home.route
            }
            isLoadingSession = false
        }

        // --- 2. GESTIÓN DE LA BARRA INFERIOR ---
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val bottomBarScreens = listOf(
            Screen.Home,
            Screen.Projects,
            Screen.Tasks,
            Screen.Profile
        )

        if (isLoadingSession) {
            // Pantalla de espera mientras se verifica Supabase
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    // Solo mostramos la barra si estamos en una pantalla principal
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
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                // --- 3. CONTENEDOR DE NAVEGACIÓN ---
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(innerPadding) // Evita que el contenido quede bajo la barra
                ) {
                    // --- FLUJO DE AUTENTICACIÓN ---
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

                    // --- FLUJO DE LA APLICACIÓN ---
                    composable(Screen.Home.route) {
                        HomeScreen()
                    }

                    composable(Screen.Projects.route) {
                        ProjectsScreen()
                    }

                    composable(Screen.Tasks.route) {
                        // Placeholder si aún no tienes esta pantalla creada
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Agenda y Tareas")
                        }
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onLogout = {
                                viewModelAuth.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}