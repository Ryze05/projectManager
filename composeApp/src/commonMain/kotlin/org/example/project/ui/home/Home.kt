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
import androidx.compose.material.icons.filled.CheckCircle
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

@Composable
fun HomeScreen() {
    var userName by remember { mutableStateOf("Usuario") }

    // EFECTO para cargar el nombre al iniciar la pantalla
    LaunchedEffect(Unit) {
        val repo = AuthRepository() // O usa tu viewModel si ya tienes uno
        val name = repo.getCurrentUserName()
        if (name != null) userName = name
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6FA))
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            HomeHeader(userName)

            Spacer(modifier = Modifier.height(24.dp))


            Text(
                text = "Resumen de progreso",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProgressSummaryCard()

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Sección Proyectos Activos
            SectionHeader(title = "Proyectos activos", actionText = "Ver todos")
            Spacer(modifier = Modifier.height(12.dp))
            ActiveProjectsRow()

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Sección Actividad Reciente
            Text(
                text = "Actividad reciente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            RecentActivityList()

            // Espacio extra al final para que no tape el BottomBar
            Spacer(modifier = Modifier.height(80.dp))

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
fun SectionHeader(title: String, actionText: String) {
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
        TextButton(onClick = { /* TODO */ }) {
            Text(text = actionText, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ActiveProjectsRow() {
    // Datos Dummy
    val projects = listOf(
        ProjectUiModel("Rediseño Web", "Vence en 3 días", 0.7f, Color(0xFF2563EB), Color(0xFFEFF4FF)),
        ProjectUiModel("App Móvil Fitness", "Vence en 1 sem", 0.4f, Color(0xFFFF8A65), Color(0xFFFFF1EE)),
        ProjectUiModel("Dashboard Admin", "Vence en 2 días", 0.9f, Color(0xFF66BB6A), Color(0xFFE8F5E9))
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        items(projects) { project ->
            ProjectCard(project)
        }
    }
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