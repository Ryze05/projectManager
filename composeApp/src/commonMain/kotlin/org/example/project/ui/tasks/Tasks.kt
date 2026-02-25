package org.example.project.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.auth
import org.example.project.network.SupabaseClient
import org.example.project.ui.components.task.AgendaTaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: TasksViewModel) {
    val state by viewModel.state
    val currentUserId = remember { SupabaseClient.client.auth.currentUserOrNull()?.id ?: "" }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.loadTasks(currentUserId)
        }
    }

    var expanded by remember { mutableStateOf(false) }

    val projectNames = remember(state.tasks) {
        listOf("Todos los proyectos") + state.tasks.mapNotNull { it.projectTitle }.distinct()
    }

    var selectedProjectFilter by remember { mutableStateOf("Todos los proyectos") }

    val groupedTasks = remember(state.tasks, selectedProjectFilter) {
        val filtered = if (selectedProjectFilter == "Todos los proyectos") {
            state.tasks
        } else {
            state.tasks.filter { it.projectTitle == selectedProjectFilter }
        }
        filtered.groupBy { it.projectTitle ?: "Otros Proyectos" }
    }

    Scaffold(
        containerColor = Color(0xFFF5F6FA)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp)) {
            Text(
                text = if (viewModel.currentSectionId == 0L) "Mi Agenda" else "Tareas",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
            Text("Gestiona tus responsabilidades personales", color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            if (projectNames.size > 1 && viewModel.currentSectionId == 0L) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedProjectFilter,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        projectNames.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedProjectFilter = name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else if (state.tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No tienes tareas pendientes 🎉", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    groupedTasks.forEach { (project, tasks) ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(4.dp, 16.dp).background(Color(0xFF2563EB), CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = project.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(tasks, key = { it.id ?: 0L }) { task ->
                            AgendaTaskItem(
                                task = task,
                                onCheckedChange = { viewModel.onTaskChecked(task) }
                            )
                        }
                    }
                }
            }
        }
    }
}