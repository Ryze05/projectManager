package org.example.project.ui.tasks

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.project.domain.models.ProjectTitleRelation
import org.example.project.domain.models.SectionRelation
import org.example.project.domain.models.Task
import org.example.project.domain.models.Task2
import org.example.project.repository.TaskRepository

data class TasksState(
    val tasks: List<Task2> = emptyList(),
    val isLoading: Boolean = true
)

class TasksViewModel(val currentSectionId: Long) : ViewModel() {

    private val repo = TaskRepository()
    private val _state = mutableStateOf(TasksState())
    val state: State<TasksState> = _state

    fun loadTasks(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val list: List<Task2> = if (currentSectionId == 0L) {
                repo.getProjectSectionsWithTasksAgenda(userId)
            } else {
                repo.getTasksBySectionFiltered(currentSectionId, userId)
            }

            _state.value = TasksState(tasks = list, isLoading = false)
        }
    }

    fun onTaskChecked(task: Task2) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            val updatedList = _state.value.tasks.map {
                if (it.id == task.id) it.copy(isCompleted = newStatus) else it
            }
            _state.value = _state.value.copy(tasks = updatedList)

            task.id?.let { repo.toggleTaskCompletion(it, newStatus) }
        }
    }

    /*fun createTask(title: String, description: String, priority: String, dueDate: String) {
        viewModelScope.launch {
            if (currentSectionId != 0L) {
                try {
                    repo.createTask(
                        title = title,
                        description = description.ifBlank { null },
                        sectionId = currentSectionId,
                        priority = priority,
                        dueDate = dueDate.ifBlank { null }
                    )
                    loadTasks()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }*/
}