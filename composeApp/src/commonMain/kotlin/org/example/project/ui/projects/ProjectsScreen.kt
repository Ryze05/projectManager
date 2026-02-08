package org.example.project.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.example.project.repository.AuthRepository
import org.example.project.ui.components.projects.ProjectCard

@Composable
fun ProjectsScreen(
    viewModel: ProjectViewModel = viewModel(),
    authRepository: AuthRepository,
    navController: NavController
) {

    var showDialog by remember { mutableStateOf(false) }
    var newProjectTitle by remember { mutableStateOf("") }

    var selectedTab by remember { mutableIntStateOf(0) }
    val state by viewModel.state.collectAsState()
    val tabs = listOf("Activos", "Completados", "Archivados")

    //TODO Optimizar cambios de proyectos entre tabs
    val currentStatus = when (selectedTab) {
        0 -> "active"
        1 -> "completed"
        else -> "archived"
    }

    LaunchedEffect(selectedTab) {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            viewModel.loadProjectsByUserAndStatus(userId, currentStatus)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (state.isAdmin) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear")
                }
            }
        },
        containerColor = Color(0xFFF5F6FA)
    ) { padding ->

        if (showDialog && state.isAdmin) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nuevo Proyecto", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Introduce el nombre del proyecto:")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newProjectTitle,
                            onValueChange = { newProjectTitle = it },
                            placeholder = { Text("Ej: Rediseño Web") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        onClick = {
                            if (newProjectTitle.isNotBlank()) {
                                val userId = authRepository.getCurrentUserId()
                                if (userId != null) {
                                    viewModel.createProject(newProjectTitle, userId, currentStatus)
                                }
                                newProjectTitle = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Crear Proyecto")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Proyectos",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF2563EB)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) Color(0xFF2563EB) else Color.Gray,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(state.projects) { project ->
                        ProjectCard(project, navController)
                    }
                }
            }
        }
    }
}