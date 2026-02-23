package org.example.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import org.example.project.domain.models.Project
import org.example.project.repository.AuthRepository
import org.example.project.repository.ProjectRepository // Añadido
import org.example.project.ui.auth.AuthViewModel
import org.example.project.ui.auth.LoginScreen
import org.example.project.ui.auth.RegisterScreen
import org.example.project.ui.chat.ChatScreen
import org.example.project.ui.chat.ChatViewModel // Añadido
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

        // --- REPOSITORIO Y ESTADOS PARA EL MENÚ DE CHAT ---
        val coroutineScope = rememberCoroutineScope()
        val projectRepository = remember { ProjectRepository() }
        var showChatMenu by remember { mutableStateOf(false) }
        var chatProjectsList by remember { mutableStateOf<List<Project>>(emptyList()) }

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

        // --- DIÁLOGO DEL MENÚ DE SALAS DE CHAT ---
        if (showChatMenu) {
            AlertDialog(
                onDismissRequest = { showChatMenu = false },
                title = { Text("¿A qué sala quieres entrar?") },
                text = {
                    if (chatProjectsList.isEmpty()) {
                        Text("No tienes proyectos activos.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(chatProjectsList) { project ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showChatMenu = false
                                            // Navegamos pasando el ID del proyecto
                                            navController.navigate("chat_screen/${project.id}")
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Text(
                                        text = "💬 Chat de ${project.title}",
                                        modifier = Modifier.padding(16.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChatMenu = false }) { Text("Cancelar") }
                },
                containerColor = Color(0xFFF5F6FA)
            )
        }

        if (isLoadingSession) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                floatingActionButton = {
                    // Muestra el botón solo en Home o Proyectos
                    if (currentRoute == Screen.Home.route || currentRoute == Screen.Projects.route) {
                        FloatingActionButton(
                            onClick = {
                                // AL PULSAR, CARGAMOS PROYECTOS Y PARAMOS EL MENÚ
                                coroutineScope.launch {
                                    chatProjectsList = projectRepository.getMyProjects()
                                    showChatMenu = true
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Comment, contentDescription = "Salas de Chat")
                        }
                    }
                },
                bottomBar = {
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
                    modifier = Modifier.padding(innerPadding)
                ) {
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

                    composable(Screen.Home.route) {
                        HomeScreen()
                    }

                    composable(Screen.Projects.route) {
                        ProjectsScreen()
                    }

                    composable(Screen.Tasks.route) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Agenda y Tareas")
                        }
                    }

                    // --- NUEVA RUTA DE CHAT CON PARÁMETRO DE PROYECTO ---
                    composable(
                        route = "chat_screen/{projectId}",
                        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L

                        // Creamos el ViewModel pasándole el projectId
                        val chatViewModel = remember(projectId) { ChatViewModel(projectId) }

                        ChatScreen(
                            viewModel = chatViewModel,
                            onBack = { navController.popBackStack() }
                        )
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