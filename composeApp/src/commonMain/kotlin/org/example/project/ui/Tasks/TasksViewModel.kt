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

class TasksViewModel(private val sectionId: Long) : ViewModel() {
    private val repo = TaskRepository()
    private val _state = mutableStateOf(TasksState())
    val state: State<TasksState> = _state

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val list = if (sectionId == 0L) {
                // Si el ID es 0, cargamos la AGENDA (todo)
                repo.getAllTasksForAgenda()
            } else {
                // Si el ID es > 0, cargamos solo esa SECCIÓN
                repo.getTasksBySection(sectionId)
            }

            _state.value = TasksState(tasks = list, isLoading = false)
        }
    }

    fun onTaskChecked(task: Task) {
        viewModelScope.launch {
            // 1. Actualización optimista (cambiamos la UI antes de que responda el servidor para que se sienta rápido)
            val updatedList = _state.value.tasks.map {
                if (it.id == task.id) it.copy(isCompleted = !it.isCompleted) else it
            }
            _state.value = _state.value.copy(tasks = updatedList)

            // 2. Llamada a la BD
            repo.toggleTaskCompletion(task.id, task.isCompleted)

            // 3. (Opcional) Recargar para asegurar
            // loadTasks()
        }
    }
}