package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.example.project.domain.models.Project
import org.example.project.repository.AuthRepository
import org.example.project.repository.ProfileRepository
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
import org.example.project.ui.profile.ProfileViewModel
import org.example.project.ui.projectDetail.ProjectDetailScreen
import org.example.project.ui.projectDetail.ProjectDetailsViewModel
import org.example.project.ui.projects.ProjectViewModel
import org.example.project.ui.taskDetail.TaskDetailScreen
import org.example.project.ui.taskDetail.TaskDetailViewModel
import org.example.project.ui.tasks.TasksScreen
import org.example.project.ui.theme.ProjectManagerTheme

@Composable
fun App(
    onPickImage: (@Composable (onImagePicked: (ByteArray) -> Unit) -> () -> Unit)? = null
) {
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
        val viewModelHome = remember { HomeViewModel(authRepository, projectRepository, sectionRepository) }

        // PROFILE
        val profileRepository = remember { ProfileRepository() }
        val viewModelProfile = remember { ProfileViewModel(authRepository, profileRepository) }

        // --- ESTADOS PARA EL MENÚ DE CHAT ---
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
                    if (bottomBarScreens.any {
                            currentRoute == it.route || currentRoute == it.targetRoute ||
                                    currentRoute?.startsWith("tasks_screen") == true
                        }) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            bottomBarScreens.forEach { screen ->

                                val isSelected = currentRoute == screen.route ||
                                        currentRoute == screen.targetRoute ||
                                        (screen == Screen.Tasks && currentRoute?.startsWith("tasks_screen") == true)

                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(screen.targetRoute) {
                                            val baseRoute = Screen.Home.route
                                            if (screen == Screen.Home) {
                                                popUpTo(baseRoute) { inclusive = false }
                                            } else {
                                                popUpTo(baseRoute) { saveState = true }
                                            }
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
                        HomeScreen(
                            viewModel = viewModelHome,
                            navController = navController,
                            authRepository = authRepository
                        )
                    }

                    composable(Screen.Projects.route) {
                        ProjectsScreen(
                            viewModel = viewModelProject,
                            authRepository = authRepository,
                            navController = navController
                        )
                    }

                    composable(Screen.Profile.route) {
                        var triggerPicker: () -> Unit = {}

                        onPickImage?.let {
                            triggerPicker = it { bytes ->
                                val userId = authRepository.getCurrentUserId()
                                if (userId != null) {
                                    viewModelProfile.uploadProfilePicture(userId, bytes)
                                }
                            }
                        }

                        ProfileScreen(
                            viewModel = viewModelProfile,
                            authRepository = authRepository,
                            onLogout = {
                                viewModelAuth.logout()
                                navController.navigate(Screen.Login.route) { popUpTo(0) }
                            },
                            onPickImage = triggerPicker
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

                    // Alias para evitar crash si algo navega a "tasks" directamente
                    composable(Screen.Tasks.route) {
                        LaunchedEffect(Unit) {
                            navController.navigate("tasks_screen/0") {
                                popUpTo(Screen.Tasks.route) { inclusive = true }
                            }
                        }
                    }

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