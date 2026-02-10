package org.example.project.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.repository.AuthRepository

// NOTA: Asegúrate de importar tu R si usas imágenes locales,
// o usa iconos temporales como he hecho yo abajo.


import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.example.project.domain.models.Project
import org.example.project.domain.models.Section


@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController // Añadimos esto para poder navegar a las tareas
) {
    val state = viewModel.state.value

    // 1. Estado para el diálogo de "Ver todos los proyectos" (Botón 'Ver todos')
    var showAllProjectsDialog by remember { mutableStateOf(false) }

    // 2. Estado para el diálogo de "Secciones" (Click en un proyecto)
    var showSectionDialog by remember { mutableStateOf(false) }

    // --- LÓGICA DE DIÁLOGOS ---

    // Diálogo A: Ver lista completa de proyectos
    if (showAllProjectsDialog) {
        AllProjectsDialog(
            projects = state.projects,
            onDismiss = { showAllProjectsDialog = false }
        )
    }

    // Diálogo B: Ver secciones de un proyecto específico
    if (showSectionDialog) {
        ProjectSectionsDialog(
            sections = state.selectedProjectSections,
            isLoading = state.isDialogLoading,
            onDismiss = { showSectionDialog = false },
            onSectionClick = { sectionId ->
                showSectionDialog = false
                // NAVEGACIÓN A PANTALLA DE TAREAS
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

        // Saludo
        HomeHeader(state.userName)

        Spacer(modifier = Modifier.height(30.dp))

        // --- ZONA REDISEÑADA (Contador Azul) ---
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
                Text("Sigue así, el trabajo duro da frutos.", color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
        }
        // ---------------------------------------

        Spacer(modifier = Modifier.height(30.dp))

        // Cabecera de Sección
        SectionHeader(
            title = "Tus Proyectos",
            actionText = "Ver todos",
            onAction = { showAllProjectsDialog = true } // Abrimos diálogo A
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Lista de Proyectos (Vertical)
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.projects.isEmpty()) {
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
                    // 1. Cargamos las secciones de este proyecto
                    viewModel.onProjectClicked(projectId)
                    // 2. Abrimos el diálogo B
                    showSectionDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ActiveProjectsColumn(projects: List<Project>,onProjectClick: (Long) -> Unit) {
    // Usamos Column en lugar de LazyRow porque el padre ya hace scroll
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        projects.forEach { project ->
            // Generamos el color (el truco del ID que hicimos antes)
            val colors = listOf(Color(0xFF2563EB), Color(0xFFFF8A65), Color(0xFF66BB6A), Color(0xFFAB47BC))
            val selectedColor = colors[(project.id % colors.size).toInt()]

            ProjectCardVertical(
                title = project.title,
                date = "Creado: ${project.createdAt.take(10)}",
                color = selectedColor,
                onClick = { onProjectClick(project.id.toLong()) }
            )
        }
    }
}
@Composable
fun ProjectCardVertical(title: String, date: String, color: Color,onClick: () -> Unit) {
    Card(onClick = onClick,
        modifier = Modifier
            .fillMaxWidth() // AHORA OCUPA TODO EL ANCHO
            .height(100.dp), // Un poco más bajita
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Franja de color a la izquierda
            Box(
                modifier = Modifier
                    .width(16.dp) // Tira vertical de color
                    .fillMaxHeight()
                    .background(color)
            )

            // Contenido
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f), // Ocupa el resto del espacio
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            // Icono de flecha o similar a la derecha (opcional)
            Icon(
                imageVector = Icons.Default.ChevronRight, // Necesitas importar este icono
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp)
            )
        }
    }
}


// --- COMPONENTES UI ---

@Composable
fun HomeHeader(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Placeholder para avatar (puedes usar Coil para cargar URL después)
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                // Aquí iría Image(...)
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Buenos días",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "Hola, $userName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .background(Color.White, CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.Black)
        }
    }
}

@Composable
fun ProgressSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progreso general",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "65%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de progreso gruesa y redondeada
            LinearProgressIndicator(
                progress = { 0.65f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "8 de 12 tareas completadas hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onAction: () -> Unit) { // Añadimos onAction
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        TextButton(onClick = onAction) { // Usamos la acción aquí
            Text(text = actionText, color = MaterialTheme.colorScheme.primary)
        }
    }
}
@Composable
fun ProjectSectionsDialog(
    sections: List<Section>, // Asegúrate de importar tu modelo Section
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSectionClick: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elige una sección") },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (sections.isEmpty()) {
                Text("Este proyecto aún no tiene secciones.")
            } else {
                // Lista vertical de secciones
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(sections) { section ->
                        Card(
                            onClick = { onSectionClick(section.id) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = section.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = Color(0xFFF5F6FA)
    )
}
@Composable
fun AllProjectsDialog(projects: List<Project>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Todos tus proyectos") },
        text = {
            // Usamos LazyColumn para que se pueda hacer scroll si hay muchos
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp), // Altura máxima
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(projects) { project ->
                    // Reutilizamos un diseño simple para la lista
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Círculo de color (truco visual)
                            val colors = listOf(Color(0xFF2563EB), Color(0xFFFF8A65), Color(0xFF66BB6A))
                            val color = colors[(project.id % colors.size).toInt()]

                            Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(project.title, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        containerColor = Color(0xFFF5F6FA)
    )
}



@Composable
fun ProjectCard(project: ProjectUiModel) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Área de imagen/color superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa la mitad superior
                    .background(project.bgColor)
            ) {
                // Aquí podrías poner una Image real. Usamos un box decorativo.
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                )
            }

            // Área de información inferior
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = project.dueDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { project.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = project.color,
                    trackColor = project.color.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
fun RecentActivityList() {
    val activities = listOf(
        ActivityUiModel("Marcos completó \"Diseño de Login\"", "Hace 15 minutos • Rediseño Web", Icons.Default.CheckCircle, Color(0xFFE3F2FD), Color(0xFF2196F3)),
        ActivityUiModel("Nuevo comentario de Sofía", "Hace 2 horas • App Fitness", Icons.Default.Comment, Color(0xFFE8F5E9), Color(0xFF4CAF50)),
        ActivityUiModel("Fecha de entrega actualizada", "Ayer • Campaña Q4", Icons.Default.Warning, Color(0xFFFFF3E0), Color(0xFFFF9800))
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        activities.forEach { activity ->
            ActivityItem(activity)
        }
    }
}

@Composable
fun ActivityItem(activity: ActivityUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat look
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(activity.iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = activity.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = activity.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = true,
            onClick = { /* TODO */ },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Folder, contentDescription = "Proyectos") },
            label = { Text("Proyectos") },
            selected = false,
            onClick = { /* TODO */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = false,
            onClick = { /* TODO */ }
        )
    }
}

// --- DATA CLASSES (Dummy) ---
data class ProjectUiModel(
    val title: String,
    val dueDate: String,
    val progress: Float,
    val color: Color,
    val bgColor: Color
)
data class ActivityUiModel(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconColor: Color
)