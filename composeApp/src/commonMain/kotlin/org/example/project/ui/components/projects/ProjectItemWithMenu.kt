package org.example.project.ui.components.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.example.project.domain.models.Project
import org.example.project.repository.AuthRepository
import org.example.project.ui.projects.ProjectViewModel

@Composable
fun ProjectItemWithMenu(
    project: Project,
    isAdmin: Boolean,
    currentStatus: String,
    authRepository: AuthRepository,
    viewModel: ProjectViewModel,
    navController: NavController
) {
    var showOptions by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(project.title) }
    var editedStatus by remember { mutableStateOf(project.status) }
    val userId = authRepository.getCurrentUserId() ?: ""

    Box(modifier = Modifier.fillMaxWidth()) {
        ProjectCard(project, navController)

        if (isAdmin) {
            IconButton(
                onClick = { showOptions = true },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = Color.Gray)
            }

            DropdownMenu(
                expanded = showOptions,
                onDismissRequest = { showOptions = false },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .width(180.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                DropdownMenuItem(
                    text = {
                        Text("Editar proyecto", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        showOptions = false
                        showEditDialog = true
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                DropdownMenuItem(
                    text = {
                        Text("Eliminar Proyecto", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        showOptions = false
                        viewModel.deleteProject(project.id!!, userId, currentStatus)
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Proyecto", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Nombre del proyecto") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Column {
                        Text("Estado", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("active" to "Activo", "completed" to "Hecho", "archived" to "Archivado")
                                .forEach { (key, label) ->
                                    FilterChip(
                                        selected = editedStatus == key,
                                        onClick = { editedStatus = key },
                                        label = { Text(label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFDBEAFE),
                                            selectedLabelColor = Color(0xFF2563EB)
                                        )
                                    )
                                }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProject(project.id!!, editedTitle, editedStatus, userId, currentStatus)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            }
        )
    }
}