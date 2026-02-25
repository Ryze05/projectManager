package org.example.project.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
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
import org.example.project.ui.components.projects.ProjectItemWithMenu

@Composable
fun ProjectsScreen(
    viewModel: ProjectViewModel = viewModel(),
    authRepository: AuthRepository,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val tabs = listOf("Activos", "Completados", "Archivados")

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var newProjectTitle by remember { mutableStateOf("") }

    val currentStatus = when (selectedTab) {
        0 -> "active"
        1 -> "completed"
        else -> "archived"
    }

    LaunchedEffect(selectedTab) {
        authRepository.getCurrentUserId()?.let { userId ->
            viewModel.loadProjectsByUserAndStatus(userId, currentStatus)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (state.isAdmin) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    // CAMBIO: Color primario adaptativo
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (showDialog && state.isAdmin) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nuevo Proyecto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Column {
                        Text("Introduce el nombre del proyecto:", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newProjectTitle,
                            onValueChange = { newProjectTitle = it },
                            placeholder = { Text("Ej: Rediseño Web") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            if (newProjectTitle.isNotBlank()) {
                                authRepository.getCurrentUserId()?.let { userId ->
                                    viewModel.createProject(newProjectTitle, userId, currentStatus)
                                }
                                newProjectTitle = ""
                                showDialog = false
                            }
                        }
                    ) { Text("Crear Proyecto", color = MaterialTheme.colorScheme.onPrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Proyectos",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Gestiona tus frentes abiertos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
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
                                color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (state.error != null) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.projects) { project ->
                            ProjectItemWithMenu(
                                project = project,
                                isAdmin = state.isAdmin,
                                currentStatus = currentStatus,
                                authRepository = authRepository,
                                viewModel = viewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}