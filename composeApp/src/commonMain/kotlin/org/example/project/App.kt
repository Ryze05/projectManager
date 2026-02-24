package org.example.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import org.example.project.repository.ProjectRepository
import org.example.project.repository.SectionRepository
import org.example.project.repository.TaskRepository
import org.example.project.ui.Tasks.TasksViewModel
import org.example.project.ui.auth.AuthViewModel
import org.example.project.ui.auth.LoginScreen
import org.example.project.ui.auth.RegisterScreen
import org.example.project.ui.components.chat.ChatScreen
import org.example.project.ui.components.chat.ChatViewModel
import org.example.project.ui.home.HomeScreen
import org.example.project.ui.home.HomeViewModel
import org.example.project.ui.projects.ProjectsScreen
import org.example.project.ui.profile.ProfileScreen
import org.example.project.ui.navigation.Screen
import org.example.project.ui.projectDetail.ProjectDetailScreen
import org.example.project.ui.projectDetail.ProjectDetailsViewModel
import org.example.project.ui.projects.ProjectViewModel
import org.example.project.ui.taskDetail.TaskDetailScreen
import org.example.project.ui.taskDetail.TaskDetailViewModel
import org.example.project.ui.tasks.TasksScreen
import org.example.project.ui.theme.ProjectManagerTheme

@Composable
fun App() {
    ProjectManagerTheme {
        val navController = rememberNavController()

        // AUTH
        val authRepository = remember { AuthRepository() }
        val viewModelAuth = remember { AuthViewModel(authRepository) }

        // PROJECTS
        val projectRepository = remember { ProjectRepository() }
        val viewModelProject = remember { ProjectViewModel(projectRepository, authRepository) }

        // PROJECT DETAIL
        val sectionRepository = remember { SectionRepository() }
        val taskRepository = remember { TaskRepository() }
        val viewModelProjectDetail = remember { ProjectDetailsViewModel(projectRepository, sectionRepository, taskRepository, authRepository) }

        // TASK DETAIL
        val viewModelTaskDetail = remember { TaskDetailViewModel(taskRepository, projectRepository, authRepository) }

        // HOME
        val viewModelHome = remember {
            HomeViewModel(
                authRepository,
                projectRepository,
                sectionRepository
            )
        }

        // --- ESTADOS PARA EL MENÚ DE CHAT ---
        val coroutineScope = rememberCoroutineScope()
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

        if (isLoadingSession) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (bottomBarScreens.any { it.route == currentRoute } || currentRoute?.startsWith("tasks_screen") == true) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            bottomBarScreens.forEach { screen ->
                                // Comprobamos si el botón actual es el de la Agenda
                                val isTasksScreen = screen == Screen.Tasks

                                // Mantenemos el icono encendido si estamos en la agenda
                                val isSelected = if (isTasksScreen) {
                                    currentRoute?.startsWith("tasks_screen") == true || currentRoute == screen.route
                                } else {
                                    currentRoute == screen.route
                                }

                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = isSelected,
                                    onClick = {
                                        // AQUI ESTÁ LA MAGIA: Si pulsamos en Agenda, forzamos la ruta con el ID 0
                                        val targetRoute = if (isTasksScreen) "tasks_screen/0" else screen.route

                                        navController.navigate(targetRoute) {
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
                        HomeScreen(viewModel = viewModelHome, navController = navController, authRepository = authRepository)
                    }

                    composable(Screen.Projects.route) {
                        ProjectsScreen(
                            viewModel = viewModelProject,
                            authRepository = authRepository,
                            navController = navController
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

                    composable(
                        route = Screen.ProjectDetails.route,
                        arguments = listOf(
                            navArgument("projectId") { type = NavType.LongType },
                            navArgument("projectName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getLong("projectId") ?: 0L
                        val name = backStackEntry.arguments?.getString("projectName") ?: "Proyecto"

                        ProjectDetailScreen(
                            projectId = id,
                            projectName = name,
                            onBack = { navController.popBackStack() },
                            viewModel = viewModelProjectDetail,
                            onTaskClick = { taskId, projName ->
                                navController.navigate(Screen.TaskDetails.createRoute(taskId, id, projName))
                            }
                        )
                    }

                    composable(
                        route = Screen.TaskDetails.route,
                        arguments = listOf(
                            navArgument("taskId") { type = NavType.LongType },
                            navArgument("projectId") { type = NavType.LongType },
                            navArgument("projectName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
                        val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                        val projectName = backStackEntry.arguments?.getString("projectName") ?: "Proyecto"

                        TaskDetailScreen(
                            taskId = taskId,
                            projectId = projectId,
                            projectName = projectName,
                            viewModel = viewModelTaskDetail,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "chat_screen/{projectId}",
                        arguments = listOf(navArgument("projectId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                        val chatViewModel = remember(projectId) { ChatViewModel(projectId) }

                        ChatScreen(
                            viewModel = chatViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // ¡AQUI ESTABA EL ERROR DEL CRASH! La ruta debe ser explícita con {sectionId}
                    composable(
                        route = "tasks_screen/{sectionId}",
                        arguments = listOf(navArgument("sectionId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val sectionId = backStackEntry.arguments?.getLong("sectionId") ?: 0L
                        val viewModel = remember(sectionId) { TasksViewModel(sectionId) }
                        TasksScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}