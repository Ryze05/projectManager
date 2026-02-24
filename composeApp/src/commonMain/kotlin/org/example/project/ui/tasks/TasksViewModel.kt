package org.example.project.ui.Tasks

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.project.domain.models.Task
import org.example.project.repository.TaskRepository

data class TasksState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true
)

// CAMBIO CLAVE: Cambiamos 'private val' por 'val' para poder leer currentSectionId en la UI
class TasksViewModel(val currentSectionId: Long) : ViewModel() {

    private val repo = TaskRepository()
    private val _state = mutableStateOf(TasksState())
    val state: State<TasksState> = _state

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val list = if (currentSectionId == 0L) {
                repo.getAllTasksForAgenda()
            } else {
                repo.getTasksBySection(currentSectionId)
            }

            _state.value = TasksState(tasks = list, isLoading = false)
        }
    }

    fun onTaskChecked(task: Task) {
        viewModelScope.launch {
            val updatedList = _state.value.tasks.map {
                if (it.id == task.id) it.copy(isCompleted = !it.isCompleted) else it
            }
            _state.value = _state.value.copy(tasks = updatedList)

            // Pasamos el ID y el nuevo valor invertido
            task.id?.let {
                repo.toggleTaskCompletion(it, !task.isCompleted)
            }
        }
    }

    // --- NUEVA FUNCIÓN PARA CREAR LA TAREA ---
    fun createTask(title: String, description: String, priority: String, dueDate: String) {
        viewModelScope.launch {
            // Solo creamos si estamos dentro de una sección real
            if (currentSectionId != 0L) {
                _state.value = _state.value.copy(isLoading = true)

                // Si dejaron esto en blanco, lo guardamos como null
                val finalDesc = if (description.isBlank()) null else description
                val finalDate = if (dueDate.isBlank()) null else dueDate

                try {
                    repo.createTask(
                        title = title,
                        description = finalDesc,
                        sectionId = currentSectionId,
                        priority = priority,
                        dueDate = finalDate
                    )
                    // Recargamos la lista automáticamente para que aparezca
                    loadTasks()
                } catch (e: Exception) {
                    e.printStackTrace()
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }
}