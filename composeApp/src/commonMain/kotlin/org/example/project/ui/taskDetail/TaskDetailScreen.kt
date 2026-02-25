package org.example.project.ui.taskDetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.components.task.DetailInfoCard
import org.example.project.ui.components.task.MemberRowItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long,
    projectId: Long,
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit,
    projectName: String
) {
    val state by viewModel.state.collectAsState()
    var showMemberSelector by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        viewModel.loadTaskData(taskId, projectId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Tarea", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (state.isAdmin) {
                        IconButton(onClick = { showMemberSelector = true }) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = "Asignar miembro",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            state.task?.let { task ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = if (task.isCompleted) "Finalizada" else "En progreso",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "• $projectName",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailInfoCard(
                                icon = Icons.Default.CalendarMonth,
                                label = "Fecha de entrega",
                                value = task.dueDate?.take(10) ?: "Sin fecha",
                                iconColor = MaterialTheme.colorScheme.primary,
                                iconBg = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            DetailInfoCard(
                                icon = Icons.Default.PriorityHigh,
                                label = "Prioridad",
                                value = task.priority.replaceFirstChar { it.uppercase() },
                                iconColor = MaterialTheme.colorScheme.error,
                                iconBg = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Column {
                            Text(
                                "Descripción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = task.description ?: "Sin descripción.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    item {
                        Column {
                            Text(
                                "Miembros asignados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(Modifier.height(12.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column {
                                    task.profiles.forEachIndexed { index, profile ->
                                        MemberRowItem(profile)
                                        if (index < task.profiles.size - 1) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                                        }
                                    }
                                    if (task.profiles.isEmpty()) {
                                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                            Text(
                                                "No hay miembros asignados",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showMemberSelector && state.isAdmin) {
                    AlertDialog(
                        onDismissRequest = { showMemberSelector = false },
                        shape = RoundedCornerShape(28.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        title = {
                            Column {
                                Text(
                                    text = "Asignar a la tarea",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${state.task?.profiles?.size ?: 0} miembros asignados",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                                Text(
                                    text = "Gestiona quién trabaja en esta tarea.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                if (state.projectMembers.isEmpty()) {
                                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                        Text("No hay miembros en el proyecto", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(state.projectMembers, key = { it.id }) { member ->
                                            val isAssigned = state.task?.profiles?.any { it.id == member.id } == true

                                            Surface(
                                                onClick = { if (isAssigned) viewModel.unassignMember(member.id) else viewModel.assignMember(member.id) },
                                                shape = RoundedCornerShape(16.dp),
                                                color = if (isAssigned) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(
                                                    width = 1.dp,
                                                    color = if (isAssigned) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        modifier = Modifier.size(40.dp),
                                                        shape = CircleShape,
                                                        color = if (isAssigned) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primaryContainer
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            if (!member.avatarUrl.isNullOrEmpty()) {
                                                                coil3.compose.AsyncImage(
                                                                    model = member.avatarUrl,
                                                                    contentDescription = "Avatar de ${member.fullName}",
                                                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                                )
                                                            } else {
                                                                Text(
                                                                    text = member.fullName.take(1).uppercase(),
                                                                    style = MaterialTheme.typography.titleMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isAssigned) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onPrimaryContainer
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Spacer(Modifier.width(12.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = member.fullName,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isAssigned) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = member.email,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }

                                                    if (isAssigned) {
                                                        Icon(Icons.Default.Close, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                                                    } else {
                                                        Icon(Icons.Default.Add, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showMemberSelector = false }) {
                                Text("Cerrar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }
        }
    }
}