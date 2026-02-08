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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
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
    onBack: () -> Unit
) {

    val state by viewModel.state.collectAsState()
    var showMemberSelector by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        viewModel.loadTaskData(taskId, projectId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tarea Detalle", fontWeight = FontWeight.Bold) },
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
                                tint = Color(0xFF2563EB)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F6FA))
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            state.task?.let {
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
                                text = it.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFDBEAFE),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    //TODO AÑADIR EN LA BS
                                    Text(
                                        text = "En progreso",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("• Proyecto Alpha", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
                                value = it.dueDate?.take(10) ?: "Sin fecha",
                                iconColor = Color(0xFF2563EB),
                                iconBg = Color(0xFFEFF6FF),
                                modifier = Modifier.weight(1f)
                            )
                            DetailInfoCard(
                                icon = Icons.Default.PriorityHigh,
                                label = "Prioridad",
                                value = it.priority.replaceFirstChar { it.uppercase() },
                                iconColor = Color(0xFFEF4444),
                                iconBg = Color(0xFFFEF2F2),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Column {
                            Text("Descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = it.description ?: "Sin descripción.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    item {
                        Column {
                            Text("Miembros asignados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column {
                                    it.profiles.forEachIndexed { index, profile ->
                                        MemberRowItem(profile)
                                        if (index < it.profiles.size - 1) {
                                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                        }
                                    }
                                    if (it.profiles.isEmpty()) {
                                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                            Text("No hay miembros asignados", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
                        containerColor = Color.White,
                        title = {
                            Text(
                                text = "Asignar a la tarea",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                                Text(
                                    text = "Selecciona un miembro del proyecto para asignarlo a esta tarea.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                if (state.projectMembers.isEmpty()) {
                                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                        Text("No hay miembros en el proyecto", color = Color.LightGray)
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(state.projectMembers.size) { index ->
                                            val member = state.projectMembers[index]
                                            val isAlreadyAssigned = state.task?.profiles?.any { it.id == member.id } == true

                                            Surface(
                                                onClick = {
                                                    if (!isAlreadyAssigned) {
                                                        viewModel.assignMember(member.id, projectId)
                                                        showMemberSelector = false
                                                    }
                                                },
                                                shape = RoundedCornerShape(16.dp),
                                                // Si ya está asignado, lo ponemos más oscuro/transparente
                                                color = if (isAlreadyAssigned) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                                                border = BorderStroke(
                                                    width = 1.dp,
                                                    color = if (isAlreadyAssigned) Color.Transparent else Color(0xFFE2E8F0)
                                                ),
                                                enabled = !isAlreadyAssigned
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Avatar con lógica de color según estado
                                                    Surface(
                                                        modifier = Modifier.size(40.dp),
                                                        shape = CircleShape,
                                                        color = if (isAlreadyAssigned) Color.LightGray else Color(0xFFDBEAFE)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = member.fullName.take(1).uppercase(),
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isAlreadyAssigned) Color.White else Color(0xFF2563EB)
                                                            )
                                                        }
                                                    }

                                                    Spacer(Modifier.width(12.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = member.fullName,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isAlreadyAssigned) Color.Gray else Color.Black
                                                        )
                                                        Text(
                                                            text = member.email,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = Color.Gray
                                                        )
                                                    }

                                                    if (isAlreadyAssigned) {
                                                        Surface(
                                                            color = Color(0xFFE2E8F0),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text(
                                                                text = "Asignado",
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.DarkGray
                                                            )
                                                        }
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
                                Text("Cerrar", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }
                    )
                }
            }
        }
    }
}