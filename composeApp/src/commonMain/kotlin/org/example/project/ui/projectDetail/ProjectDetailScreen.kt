package org.example.project.ui.projectDetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.example.project.network.SupabaseClient
import org.example.project.ui.components.ProjectDetail.ProjectSectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    projectName: String,
    viewModel: ProjectDetailsViewModel = viewModel(),
    onBack: () -> Unit,
    onTaskClick: (Long, String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddSectionDialog by remember { mutableStateOf(false) }
    var newSectionName by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("media") }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedSectionId by remember { mutableStateOf<Long?>(null) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("media") }

    var showEditTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<org.example.project.domain.models.Task?>(null) }
    var editTaskTitle by remember { mutableStateOf("") }
    var editTaskDescription by remember { mutableStateOf("") }
    var editTaskPriority by remember { mutableStateOf("media") }

    var showInviteDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = 23, initialMinute = 59)
    var showTimePicker by remember { mutableStateOf(false) }

    var showEditSectionDialog by remember { mutableStateOf(false) }
    var sectionToEdit by remember { mutableStateOf<org.example.project.domain.models.Section?>(null) }
    var editSectionName by remember { mutableStateOf("") }
    var editSectionPriority by remember { mutableStateOf("media") }

    val priorities = listOf("baja", "media", "alta")
    val currentUserId = remember { SupabaseClient.client.auth.currentUserOrNull()?.id ?: "" }

    LaunchedEffect(projectId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.loadProjectContent(projectId, projectName, currentUserId)
        }
    }

    fun combineDateTimeToIso(dateMillis: Long?, hour: Int, minute: Int): String? {
        if (dateMillis == null) return null
        val date = Instant.fromEpochMilliseconds(dateMillis).toLocalDateTime(TimeZone.UTC).date
        val localDateTime = kotlinx.datetime.LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minute, 0)
        return localDateTime.toInstant(TimeZone.currentSystemDefault()).toString()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = state.projectName, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (state.isAdmin) {
                        IconButton(onClick = { showInviteDialog = true }) {
                            Icon(Icons.Default.GroupAdd, "Miembros", tint = Color(0xFF2563EB))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F6FA))
            )
        },
        floatingActionButton = {
            if (state.isAdmin) {
                FloatingActionButton(
                    onClick = { showAddSectionDialog = true },
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    shape = CircleShape
                ) { Icon(Icons.Default.Add, "Nueva Sección") }
            }
        },
        containerColor = Color(0xFFF5F6FA)
    ) { padding ->

        if (showAddSectionDialog && state.isAdmin) {
            AlertDialog(
                onDismissRequest = { showAddSectionDialog = false },
                title = { Text("Nueva Sección", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = newSectionName,
                            onValueChange = { newSectionName = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            priorities.forEach { p ->
                                FilterChip(
                                    selected = selectedPriority == p,
                                    onClick = { selectedPriority = p },
                                    label = { Text(p.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newSectionName.isNotBlank()) {
                            viewModel.addSection(newSectionName, projectId, selectedPriority)
                            showAddSectionDialog = false
                            newSectionName = ""
                        }
                    }) { Text("Crear") }
                },
                dismissButton = { TextButton(onClick = { showAddSectionDialog = false }) { Text("Cancelar") } }
            )
        }

        if (showEditSectionDialog && state.isAdmin) {
            AlertDialog(
                onDismissRequest = { showEditSectionDialog = false },
                title = { Text("Editar Sección", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = editSectionName,
                            onValueChange = { editSectionName = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            priorities.forEach { p ->
                                FilterChip(
                                    selected = editSectionPriority == p,
                                    onClick = { editSectionPriority = p },
                                    label = { Text(p.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        sectionToEdit?.let { section ->
                            section.id?.let { safeId ->
                                viewModel.updateSection(safeId, editSectionName, editSectionPriority)
                            }
                        }
                        showEditSectionDialog = false
                    }) { Text("Guardar") }
                },
                dismissButton = { TextButton(onClick = { showEditSectionDialog = false }) { Text("Cancelar") } }
            )
        }

        if (showAddTaskDialog && state.isAdmin) {
            AlertDialog(
                onDismissRequest = { showAddTaskDialog = false },
                title = { Text("Nueva Tarea", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = newTaskTitle, onValueChange = { newTaskTitle = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = newTaskDescription, onValueChange = { newTaskDescription = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = datePickerState.selectedDateMillis?.let { millis ->
                                    val date = Instant.fromEpochMilliseconds(millis)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                    "${date.dayOfMonth}/${date.monthNumber}/${date.year}"
                                } ?: "Fecha de entrega"
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            priorities.forEach { p ->
                                FilterChip(
                                    selected = newTaskPriority == p,
                                    onClick = { newTaskPriority = p },
                                    label = { Text(p.capitalize()) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            val isoDate = combineDateTimeToIso(datePickerState.selectedDateMillis, timePickerState.hour, timePickerState.minute)
                            viewModel.addTask(newTaskTitle, selectedSectionId!!, newTaskPriority, newTaskDescription, isoDate)
                            showAddTaskDialog = false
                            newTaskTitle = ""
                        }
                    }) { Text("Guardar") }
                },
                dismissButton = { TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancelar") } }
            )
        }

        if (showEditTaskDialog && state.isAdmin) {
            AlertDialog(
                onDismissRequest = { showEditTaskDialog = false },
                title = { Text("Editar Tarea", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = editTaskTitle,
                            onValueChange = { editTaskTitle = it },
                            label = { Text("Título") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = editTaskDescription,
                            onValueChange = { editTaskDescription = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                datePickerState.selectedDateMillis?.let {
                                    val date = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault())
                                    "${date.dayOfMonth}/${date.monthNumber}/${date.year}"
                                } ?: taskToEdit?.dueDate?.take(10) ?: "Cambiar fecha"
                            )
                        }

                        Text("Prioridad", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            priorities.forEach { p ->
                                FilterChip(
                                    selected = editTaskPriority == p,
                                    onClick = { editTaskPriority = p },
                                    label = { Text(p.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        onClick = {
                            taskToEdit?.let { task ->
                                // Si el usuario seleccionó una nueva fecha, la combinamos.
                                // Si no, mantenemos la original (task.dueDate)
                                val finalDate = if (datePickerState.selectedDateMillis != null) {
                                    combineDateTimeToIso(
                                        datePickerState.selectedDateMillis,
                                        timePickerState.hour,
                                        timePickerState.minute
                                    )
                                } else {
                                    task.dueDate
                                }

                                viewModel.updateTask(
                                    taskId = task.id!!,
                                    title = editTaskTitle,
                                    description = editTaskDescription,
                                    priority = editTaskPriority,
                                    isCompleted = task.isCompleted,
                                    dueDate = finalDate
                                )
                            }
                            showEditTaskDialog = false
                        }
                    ) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditTaskDialog = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }

        if (showDatePicker) {
            DatePickerDialog(onDismissRequest = { showDatePicker = false },
                confirmButton = { TextButton(onClick = { showDatePicker = false; showTimePicker = true }) { Text("Siguiente") } }
            ) { DatePicker(state = datePickerState) }
        }

        if (showTimePicker) {
            AlertDialog(onDismissRequest = { showTimePicker = false },
                confirmButton = { TextButton(onClick = { showTimePicker = false }) { Text("OK") } },
                text = { TimePicker(state = timePickerState) }
            )
        }

        if (showInviteDialog && state.isAdmin) {
            LaunchedEffect(Unit) { viewModel.loadAllProfiles() }
            AlertDialog(
                onDismissRequest = { showInviteDialog = false },
                title = { Text("Gestionar equipo", fontWeight = FontWeight.Bold) },
                text = {
                    Box(modifier = Modifier.heightIn(max = 400.dp)) {
                        LazyColumn {
                            items(state.allUsers) { user ->
                                val isMember = state.projectMembers.any { it.id == user.id }
                                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.fullName, Modifier.weight(1f))
                                    IconButton(onClick = {
                                        if (isMember) viewModel.removeMember(user.id, projectId)
                                        else viewModel.addMember(user.id, projectId)
                                    }) {
                                        Icon(if (isMember) Icons.Default.RemoveCircle else Icons.Default.AddCircle,
                                            contentDescription = null, tint = if (isMember) Color.Red else Color(0xFF2563EB))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showInviteDialog = false }) { Text("Listo") } }
            )
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF2563EB)) }
        } else {
            val filteredSections = remember(state.sections, searchQuery) {
                if (searchQuery.isBlank()) state.sections
                else state.sections.mapNotNull { section ->
                    val filteredTasks = section.task.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    if (filteredTasks.isNotEmpty()) section.copy(task = filteredTasks) else null
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar tareas...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }

                filteredSections.forEach { section ->
                    item {
                        ProjectSectionHeader(
                            section = section,
                            isAdmin = state.isAdmin,
                            onAddTask = {
                                selectedSectionId = section.id
                                showAddTaskDialog = true
                            },
                            onEditSection = {
                                sectionToEdit = section
                                editSectionName = section.name
                                editSectionPriority = section.priority
                                showEditSectionDialog = true
                            },
                            onDeleteSection = {
                                section.id?.let { safeId ->
                                    viewModel.deleteSection(safeId)
                                }
                            }
                        )
                    }

                    items(section.task) { task ->
                        val isMine = remember(task.profiles) { task.profiles.any { it.id == currentUserId } }
                        val canToggle = state.isAdmin || isMine
                        var showMenu by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .alpha(if (canToggle) 1f else 0.6f)
                                .clickable { onTaskClick(task.id!!, projectName) },
                            colors = CardDefaults.cardColors(containerColor = if (task.isCompleted) Color(0xFFF1F5F9) else Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(if (task.isCompleted) 0.dp else 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    if (canToggle) viewModel.toggleTaskStatus(task.id!!, !task.isCompleted)
                                    else scope.launch { snackbarHostState.showSnackbar("Tarea asignada a otros") }
                                }) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (canToggle) Color(0xFF2563EB) else Color(0xFF94A3B8)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = when (task.priority.lowercase()) {
                                                "alta" -> Color(0xFFFEE2E2); "media" -> Color(0xFFFEF3C7); else -> Color(0xFFDCFCE7)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = task.priority.uppercase(),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = when (task.priority.lowercase()) {
                                                    "alta" -> Color(0xFFB91C1C); "media" -> Color(0xFFB45309); else -> Color(0xFF15803D)
                                                }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(task.dueDate?.take(10) ?: "Sin fecha", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        if (isMine) {
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("• Asignada a ti", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2563EB), fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }

                                if (state.isAdmin) {
                                    Box {
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = Color.LightGray)
                                        }
                                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Editar tarea") },
                                                onClick = {
                                                    showMenu = false
                                                    taskToEdit = task
                                                    editTaskTitle = task.title
                                                    editTaskDescription = task.description ?: ""
                                                    editTaskPriority = task.priority
                                                    showEditTaskDialog = true
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Eliminar", color = Color.Red) },
                                                onClick = {
                                                    showMenu = false
                                                    viewModel.deleteTask(task.id!!)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}