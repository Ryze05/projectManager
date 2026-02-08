package org.example.project.ui.taskDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.repository.AuthRepository
import org.example.project.repository.ProjectRepository
import org.example.project.repository.TaskRepository

class TaskDetailViewModel(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _state = kotlinx.coroutines.flow.MutableStateFlow(TaskDetailState())
    val state = _state.asStateFlow()

    fun loadTaskData(taskId: Long, projectId: Long) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val profile = authRepository.getCurrentUserProfile()
                val task = taskRepository.getTaskById(taskId)
                val members = projectRepository.getProjectMembers(projectId)

                _state.update { it.copy(task = task, projectMembers = members, isLoading = false, isAdmin = profile?.isAdmin ?: false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun assignMember(profileId: String, projectId: Long) {
        if (!_state.value.isAdmin) return
        val currentTaskId = _state.value.task?.id ?: return
        viewModelScope.launch {
            try {
                taskRepository.assignUserToTask(currentTaskId, profileId)
                loadTaskData(currentTaskId, projectId)
            } catch (e: Exception) {
                // ESTO ES CLAVE: Imprime el error real en la consola
                println("ERROR_ASSIGN_MEMBER: ${e.message}")
                e.printStackTrace()
                _state.update { it.copy(error = "Error al asignar: ${e.message}") }
            }
        }
    }
}