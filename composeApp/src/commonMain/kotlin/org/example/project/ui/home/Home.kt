package org.example.project.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.example.project.domain.models.Project
import org.example.project.repository.AuthRepository
import org.example.project.ui.components.home.ActiveProjectsColumn
import org.example.project.ui.components.home.HomeHeader
import org.example.project.ui.components.home.SectionHeader
import org.example.project.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController,
    authRepository: AuthRepository
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            viewModel.loadHomeData(userId)
        }
    }

    var showSectionDialog by remember { mutableStateOf(false) }
    var showChatMenu by remember { mutableStateOf(false) }
    var chatProjectsList by remember { mutableStateOf(emptyList<Project>()) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        chatProjectsList = viewModel.getMyProjectsForChat()
                        showChatMenu = true
                    }
                },
                containerColor = Color(0xFF2563EB)
            ) {
                Icon(Icons.Default.Comment, contentDescription = "Chat", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {

            if (!state.isLoading && state.userName != "Cargando...") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    HomeHeader(state.userName, state.avatarUrl,false)

                    Spacer(modifier = Modifier.height(30.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Proyectos en marcha", color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = "${state.projects.size}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (state.isAdmin) "Panel de Administrador activo" else "Sigue así, el trabajo duro da frutos.",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    SectionHeader(
                        title = "Tus Proyectos",
                        actionText = "Ver todos",
                        onAction = {
                            navController.navigate(Screen.Projects.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.projects.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay proyectos aún 📂", color = Color.Gray)
                        }
                    } else {
                        ActiveProjectsColumn(
                            projects = state.projects,
                            onProjectClick = { projectId ->
                                viewModel.onProjectClicked(projectId)
                                showSectionDialog = true
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            if (state.isLoading || state.userName == "Cargando...") {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F6FA)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2563EB))
                    }
                }
            }

            if (showSectionDialog) {
                AlertDialog(
                    onDismissRequest = { showSectionDialog = false },
                    title = { Text("Elige una sección", fontWeight = FontWeight.ExtraBold) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (state.isDialogLoading) {
                                Box(Modifier.fillMaxWidth().height(150.dp), Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF2563EB))
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 350.dp)) {
                                    items(state.selectedProjectSections) { section ->
                                        Card(
                                            onClick = {
                                                showSectionDialog = false
                                                navController.navigate("tasks_screen/${section.id}")
                                            },
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(2.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                                Text(section.name, fontWeight = FontWeight.Bold)
                                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, Modifier.size(16.dp), tint = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSectionDialog = false }) {
                            Text("Cancelar", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = Color(0xFFF5F6FA),
                    shape = RoundedCornerShape(28.dp)
                )
            }

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
                        TextButton(onClick = { showChatMenu = false }) {
                            Text("Cancelar")
                        }
                    },
                    containerColor = Color(0xFFF5F6FA)
                )
            }
        }
    }
}