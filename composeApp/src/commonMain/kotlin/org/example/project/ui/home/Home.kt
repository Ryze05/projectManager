package org.example.project.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.example.project.domain.models.Project
import org.example.project.domain.models.Section

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()

    var showAllProjectsDialog by remember { mutableStateOf(false) }
    var showSectionDialog by remember { mutableStateOf(false) }

    // --- DIÁLOGOS ---
    if (showAllProjectsDialog) {
        AllProjectsDialog(
            projects = state.projects,
            onDismiss = { showAllProjectsDialog = false },
            onProjectClick = { projectId ->
                showAllProjectsDialog = false
                viewModel.onProjectClicked(projectId)
                showSectionDialog = true
            }
        )
    }

    if (showSectionDialog) {
        ProjectSectionsDialog(
            sections = state.selectedProjectSections,
            isLoading = state.isDialogLoading,
            onDismiss = { showSectionDialog = false },
            onSectionClick = { sectionId ->
                showSectionDialog = false
                navController.navigate("tasks_screen/$sectionId")
            }
        )
    }

    // --- INTERFAZ PRINCIPAL ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        HomeHeader(
            userName = state.userName,
            onProfileClick = { navController.navigate("profile") }
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Card de Contador Azul
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Proyectos activos", color = Color.White.copy(alpha = 0.8f))
                Text(
                    text = "${state.projects.size}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if(state.isAdmin) "Panel de control de Administrador" else "Sigue así, el trabajo duro da frutos.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        SectionHeader(
            title = "Tus Proyectos",
            actionText = "Ver todos",
            onAction = { showAllProjectsDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else if (state.projects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.White, RoundedCornerShape(12.dp)),
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

        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "Actividad reciente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        RecentActivityList()

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- COMPONENTES DE APOYO ---

@Composable
fun HomeHeader(userName: String, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp).clickable { onProfileClick() },
                shape = CircleShape,
                color = Color(0xFFDBEAFE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(userName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Hola de nuevo,", color = Color.Gray)
                Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
        IconButton(onClick = { }, modifier = Modifier.background(Color.White, CircleShape)) {
            Icon(Icons.Default.Notifications, null)
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TextButton(onClick = onAction) { Text(actionText, color = Color(0xFF2563EB)) }
    }
}

@Composable
fun ActiveProjectsColumn(projects: List<Project>, onProjectClick: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        projects.take(4).forEach { project ->
            ProjectCardVertical(
                title = project.title,
                date = "Creado: ${project.createdAt?.take(10) ?: "N/A"}", // Corregido el null safety
                color = Color(0xFF2563EB),
                onClick = { onProjectClick(project.id ?: 0L) }
            )
        }
    }
}

@Composable
fun ProjectCardVertical(title: String, date: String, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(12.dp).fillMaxHeight().background(color))
            Column(modifier = Modifier.padding(16.dp).weight(1f), verticalArrangement = Arrangement.Center) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.align(Alignment.CenterVertically).padding(end = 16.dp))
        }
    }
}

@Composable
fun AllProjectsDialog(projects: List<Project>, onDismiss: () -> Unit, onProjectClick: (Long) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tus Proyectos", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(projects) { project ->
                    ProjectCardVertical(
                        title = project.title,
                        date = "Creado: ${project.createdAt?.take(10) ?: "N/A"}",
                        color = Color(0xFF2563EB),
                        onClick = { onProjectClick(project.id ?: 0L) }
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
fun ProjectSectionsDialog(sections: List<Section>, isLoading: Boolean, onDismiss: () -> Unit, onSectionClick: (Long) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elige una sección") },
        text = {
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (sections.isEmpty()) {
                Text("No hay secciones aún.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 300.dp)) {
                    items(sections) { section ->
                        Card(
                            onClick = { onSectionClick(section.id ?: 0L) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(section.name, fontWeight = FontWeight.Bold)
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun RecentActivityList() {
    val activities = listOf(
        Pair("Nueva tarea asignada", "Hace 15 min"),
        Pair("Proyecto actualizado", "Hace 2 horas"),
        Pair("Sección creada", "Ayer")
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        activities.forEach { activity ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).background(Color(0xFFDBEAFE), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.History, null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(activity.first, fontWeight = FontWeight.SemiBold)
                        Text(activity.second, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}