package org.example.project.ui.projectDetail

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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

    //SECTION
    var selectedPriority by remember { mutableStateOf("media") }
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var newSectionName by remember { mutableStateOf("") }

    //TASK
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedSectionId by remember { mutableStateOf<Long?>(null) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("media") }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = 23, initialMinute = 59)
    var showTimePicker by remember { mutableStateOf(false) }

    //INVITAR MIEMBROS
    var showInviteDialog by remember { mutableStateOf(false) }

    val priorities = listOf("baja", "media", "alta")

    val currentUserId = remember { SupabaseClient.client.auth.currentUserOrNull()?.id ?: "" }

    LaunchedEffect(projectId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.loadProjectContent(projectId, projectName, currentUserId)
        }
    }

    fun combineDateTimeToIso(dateMillis: Long?, hour: Int, minute: Int): String? {
        if (dateMillis == null) return null

        val date = Instant.fromEpochMilliseconds(dateMillis)
            .toLocalDateTime(TimeZone.UTC).date

        val localDateTime = kotlinx.datetime.LocalDateTime(
            date.year, date.monthNumber, date.dayOfMonth, hour, minute, 0
        )

        val systemTz = TimeZone.currentSystemDefault()
        val instant = localDateTime.toInstant(systemTz)
        return instant.toString()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.projectName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    if (state.isAdmin) {
                        IconButton(onClick = { showInviteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = "Invitar miembro",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (state.isAdmin) {
                FloatingActionButton(
                    onClick = { showAddSectionDialog = true },
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

        if (showAddSectionDialog && state.isAdmin) {
            AlertDialog(
                onDismissRequest = { showAddSectionDialog = false },
                title = { Text("Nueva Sección", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = newSectionName,
                            onValueChange = { newSectionName = it },
                            label = { Text("Nombre de la sección") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Column {
                            Text("Prioridad", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                priorities.forEach { priority ->
                                    FilterChip(
                                        selected = selectedPriority == priority,
                                        onClick = { selectedPriority = priority },
                                        label = { Text(priority.replaceFirstChar { it.uppercase() }) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = when (priority) {
                                                "alta" -> MaterialTheme.colorScheme.errorContainer
                                                "media" -> MaterialTheme.colorScheme.tertiaryContainer
                                                else -> MaterialTheme.colorScheme.secondaryContainer
                                            },
                                            selectedLabelColor = when (priority) {
                                                "alta" -> MaterialTheme.colorScheme.onErrorContainer
                                                "media" -> MaterialTheme.colorScheme.onTertiaryContainer
                                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                                            },
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                            if (newSectionName.isNotBlank()) {
                                viewModel.addSection(newSectionName, projectId, selectedPriority)
                                newSectionName = ""
                                selectedPriority = "media"
                                showAddSectionDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Crear", color = MaterialTheme.colorScheme.onPrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSectionDialog = false }) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showAddTaskDialog && state.isAdmin) {
            AlertDialog(
                onDismissRequest = { showAddTaskDialog = false },
                title = { Text("Nueva Tarea", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text("Título") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newTaskDescription,
                            onValueChange = { newTaskDescription = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = datePickerState.selectedDateMillis?.let {
                                    val date = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault())
                                    "${date.dayOfMonth}/${date.monthNumber}/${date.year}"
                                } ?: "Fecha de entrega",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            priorities.forEach { priority ->
                                FilterChip(
                                    selected = newTaskPriority == priority,
                                    onClick = { newTaskPriority = priority },
                                    label = { Text(priority.replaceFirstChar { it.uppercase() }) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when (priority) {
                                            "alta" -> MaterialTheme.colorScheme.errorContainer
                                            "media" -> MaterialTheme.colorScheme.tertiaryContainer
                                            else -> MaterialTheme.colorScheme.secondaryContainer
                                        },
                                        selectedLabelColor = when (priority) {
                                            "alta" -> MaterialTheme.colorScheme.onErrorContainer
                                            "media" -> MaterialTheme.colorScheme.onTertiaryContainer
                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                        },
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                val combinedIsoDate = combineDateTimeToIso(
                                    datePickerState.selectedDateMillis,
                                    timePickerState.hour,
                                    timePickerState.minute
                                )

                                viewModel.addTask(
                                    title = newTaskTitle,
                                    sectionId = selectedSectionId!!,
                                    priority = newTaskPriority,
                                    description = newTaskDescription,
                                    dueDate = combinedIsoDate
                                )
                                showAddTaskDialog = false
                                newTaskTitle = ""; newTaskDescription = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Guardar", color = MaterialTheme.colorScheme.onPrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTaskDialog = false }) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showDatePicker) {
            androidx.compose.material3.DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        showTimePicker = true
                    }) { Text("Siguiente", color = MaterialTheme.colorScheme.primary) }
                },
                colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
            ) { DatePicker(state = datePickerState) }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("OK", color = MaterialTheme.colorScheme.primary) }
                },
                title = { Text("Selecciona la hora", color = MaterialTheme.colorScheme.onSurface) },
                text = { TimePicker(state = timePickerState) },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showInviteDialog && state.isAdmin) {
            LaunchedEffect(Unit) { viewModel.loadAllProfiles() }

            AlertDialog(
                onDismissRequest = { showInviteDialog = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Column {
                        Text(
                            text = "Gestionar equipo",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${state.projectMembers.size} miembros activos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                        Text(
                            text = "Añade o elimina miembros de este proyecto.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (state.allUsers.isEmpty()) {
                            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(state.allUsers) { user ->
                                    val isAlreadyMember = state.projectMembers.any { it.id == user.id }

                                    Surface(
                                        onClick = {
                                            if (isAlreadyMember) {
                                                viewModel.removeMember(user.id, projectId)
                                            } else {
                                                viewModel.addMember(user.id, projectId)
                                            }
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isAlreadyMember) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (isAlreadyMember) Color.Transparent else MaterialTheme.colorScheme.outline)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(40.dp),
                                                shape = CircleShape,
                                                color = if (isAlreadyMember) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = user.fullName.take(1).uppercase(),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isAlreadyMember) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = user.fullName,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAlreadyMember) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = user.email,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            if (isAlreadyMember) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Asignado",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInviteDialog = false }) {
                        Text("Listo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {

            val filteredSections = remember(state.sections, searchQuery) {
                if (searchQuery.isBlank()) {
                    state.sections
                } else {
                    state.sections.mapNotNull { section ->
                        val filteredTasks = section.task.filter { task ->
                            task.title.contains(searchQuery, ignoreCase = true) ||
                                    (task.description?.contains(
                                        searchQuery,
                                        ignoreCase = true
                                    ) == true)
                        }

                        if (filteredTasks.isNotEmpty()) {
                            section.copy(task = filteredTasks)
                        } else {
                            null
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar tareas...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (filteredSections.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No se encontraron tareas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                filteredSections.forEach {

                    item(key = "header_${it.id}") {

                        var showSectionOptions by remember { mutableStateOf(false) }
                        var showEditSectionDialog by remember { mutableStateOf(false) }

                        var editName by remember { mutableStateOf(it.name) }
                        var editPriority by remember { mutableStateOf(it.priority) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.isAdmin) {
                                IconButton(
                                    onClick = {
                                        selectedSectionId = it.id
                                        showAddTaskDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Añadir tarea",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(Modifier.width(12.dp))

                                Surface(
                                    color = when (it.priority.lowercase()) {
                                        "alta" -> MaterialTheme.colorScheme.errorContainer
                                        "media" -> MaterialTheme.colorScheme.tertiaryContainer
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = it.priority.replaceFirstChar { it.uppercase() },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (it.priority.lowercase()) {
                                            "alta" -> MaterialTheme.colorScheme.onErrorContainer
                                            "media" -> MaterialTheme.colorScheme.onTertiaryContainer
                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )
                                }
                            }

                            if (state.isAdmin) {
                                Box {
                                    IconButton(
                                        onClick = { showSectionOptions = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Opciones",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showSectionOptions,
                                        onDismissRequest = { showSectionOptions = false },
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Editar sección", color = MaterialTheme.colorScheme.onSurface) },
                                            onClick = {
                                                showSectionOptions = false
                                                showEditSectionDialog = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar sección", color = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                showSectionOptions = false
                                                it.id?.let { id -> viewModel.deleteSection(id) }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (showEditSectionDialog) {
                            AlertDialog(
                                onDismissRequest = { showEditSectionDialog = false },
                                title = { Text("Editar Sección", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        OutlinedTextField(
                                            value = editName,
                                            onValueChange = { editName = it },
                                            label = { Text("Nombre de la sección") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Column {
                                            Text("Prioridad", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                                            Spacer(Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                priorities.forEach { priority ->
                                                    FilterChip(
                                                        selected = editPriority == priority,
                                                        onClick = { editPriority = priority },
                                                        label = { Text(priority.replaceFirstChar { it.uppercase() }) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = when (priority) {
                                                                "alta" -> MaterialTheme.colorScheme.errorContainer
                                                                "media" -> MaterialTheme.colorScheme.tertiaryContainer
                                                                else -> MaterialTheme.colorScheme.secondaryContainer
                                                            },
                                                            selectedLabelColor = when (priority) {
                                                                "alta" -> MaterialTheme.colorScheme.onErrorContainer
                                                                "media" -> MaterialTheme.colorScheme.onTertiaryContainer
                                                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                                                            }
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
                                            it.id?.let { id -> viewModel.updateSection(id, editName, editPriority) }
                                            showEditSectionDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) { Text("Guardar", color = MaterialTheme.colorScheme.onPrimary) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showEditSectionDialog = false }) {
                                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }

                    items(it.task, key = { task -> task.id ?: 0L }) { task ->

                        var showTaskOptions by remember { mutableStateOf(false) }
                        var showEditTaskDialog by remember { mutableStateOf(false) }

                        var editTaskTitle by remember { mutableStateOf(task.title) }
                        var editTaskDescription by remember { mutableStateOf(task.description ?: "") }
                        var editTaskPriority by remember { mutableStateOf(task.priority) }
                        var editIsCompleted by remember { mutableStateOf(task.isCompleted) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .animateItem()
                                .clickable { onTaskClick(task.id!!, projectName) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 2.dp),
                            border = if (task.isCompleted) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (task.isCompleted && !state.isAdmin) {
                                            scope.launch { snackbarHostState.showSnackbar("Solo administradores pueden reabrir tareas") }
                                        } else {
                                            viewModel.toggleTaskStatus(task.id!!, !task.isCompleted)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Marcar tarea",
                                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(Modifier.width(8.dp))

                                        if (task.isCompleted) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "Hecho",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Prioridad: ${task.priority.replaceFirstChar { it.uppercase() }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (state.isAdmin) {
                                    Box {
                                        IconButton(onClick = { showTaskOptions = true }) {
                                            Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        DropdownMenu(
                                            expanded = showTaskOptions,
                                            onDismissRequest = { showTaskOptions = false },
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Editar tarea", color = MaterialTheme.colorScheme.onSurface) },
                                                onClick = {
                                                    showTaskOptions = false
                                                    showEditTaskDialog = true
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Eliminar tarea", color = MaterialTheme.colorScheme.error) },
                                                onClick = {
                                                    showTaskOptions = false
                                                    viewModel.deleteTask(task.id!!)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (showEditTaskDialog) {
                            AlertDialog(
                                onDismissRequest = { showEditTaskDialog = false },
                                title = { Text("Editar Tarea", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
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

                                        Text("Prioridad", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            priorities.forEach { priority ->
                                                FilterChip(
                                                    selected = editTaskPriority == priority,
                                                    onClick = { editTaskPriority = priority },
                                                    label = { Text(priority.replaceFirstChar { it.uppercase() }) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = when (priority) {
                                                            "alta" -> MaterialTheme.colorScheme.errorContainer
                                                            "media" -> MaterialTheme.colorScheme.tertiaryContainer
                                                            else -> MaterialTheme.colorScheme.secondaryContainer
                                                        },
                                                        selectedLabelColor = when (priority) {
                                                            "alta" -> MaterialTheme.colorScheme.onErrorContainer
                                                            "media" -> MaterialTheme.colorScheme.onTertiaryContainer
                                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                                        }
                                                    )
                                                )
                                            }
                                        }

                                        Text("Estado de la tarea", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            FilterChip(
                                                selected = !editIsCompleted,
                                                onClick = { editIsCompleted = false },
                                                label = { Text("Pendiente") },
                                                leadingIcon = if (!editIsCompleted) {
                                                    { Icon(Icons.Default.Close, null, Modifier.size(18.dp)) }
                                                } else null
                                            )
                                            FilterChip(
                                                selected = editIsCompleted,
                                                onClick = { editIsCompleted = true },
                                                label = { Text("Hecho") },
                                                leadingIcon = if (editIsCompleted) {
                                                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                                } else null,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            if (editTaskTitle.isNotBlank()) {
                                                viewModel.updateTask(
                                                    taskId = task.id!!,
                                                    title = editTaskTitle,
                                                    description = editTaskDescription,
                                                    priority = editTaskPriority,
                                                    isCompleted = editIsCompleted,
                                                    dueDate = task.dueDate
                                                )
                                                showEditTaskDialog = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) { Text("Guardar", color = MaterialTheme.colorScheme.onPrimary) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showEditTaskDialog = false }) {
                                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
            }
        }
    }
}