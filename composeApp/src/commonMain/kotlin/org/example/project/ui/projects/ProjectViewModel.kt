package org.example.project.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.repository.AuthRepository
import org.example.project.repository.ProjectRepository

class ProjectViewModel(
    private val projectRepository: ProjectRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _state = MutableStateFlow(ProjectState())
    val state = _state.asStateFlow()

    fun loadProjectsByUserAndStatus(profileId: String, status: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val profile = authRepository.getCurrentUserProfile()

                val projects = projectRepository.getProjectsByStatus(profileId, status)

                _state.update { it.copy(isLoading = false, projects = projects, isAdmin = profile?.isAdmin ?: false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error al conectar con la base de datos: ${e.message}") }
            }
        }
    }

    fun createProject(title: String, ownerId: String, currentStatus: String) {
        if (!_state.value.isAdmin) return
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            try {
                projectRepository.addProject(title, ownerId)
                loadProjectsByUserAndStatus(ownerId, currentStatus)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al crear: ${e.message}") }
            }
        }
    }
}