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

    fun assignMember(profileId: String) {
        val currentTask = _state.value.task ?: return
        val userToAdd = _state.value.projectMembers.find { it.id == profileId } ?: return
        val oldProfiles = currentTask.profiles

        _state.update { it.copy(
            task = currentTask.copy(profiles = oldProfiles + userToAdd)
        )}

        viewModelScope.launch {
            try {
                taskRepository.assignUserToTask(currentTask.id!!, profileId)
            } catch (e: Exception) {
                _state.update { it.copy(task = currentTask.copy(profiles = oldProfiles), error = e.message) }
            }
        }
    }

    fun unassignMember(profileId: String) {
        val currentTask = _state.value.task ?: return
        val oldProfiles = currentTask.profiles

        _state.update { it.copy(
            task = currentTask.copy(profiles = oldProfiles.filter { it.id != profileId })
        )}

        viewModelScope.launch {
            try {
                taskRepository.unassignUserFromTask(currentTask.id!!, profileId)
            } catch (e: Exception) {
                // Rollback
                _state.update { it.copy(task = currentTask.copy(profiles = oldProfiles), error = e.message) }
            }
        }
    }
}