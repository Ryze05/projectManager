package org.example.project.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.repository.ProjectRepository

class ProjectViewModel(private val projectRepository: ProjectRepository): ViewModel() {
    private val _state = MutableStateFlow(ProjectState())
    val state = _state.asStateFlow()

    //Todos los proyectos
    fun loadAllProjects() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val projects = projectRepository.getProjectsMember()
                _state.update { it.copy(isLoading = false, projects = projects) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    //Projectos donde el Usuario es miembro
    fun loadProjectsByUserAndStatus(profileId: String, status: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val projects = projectRepository.getProjectsByStatus(profileId, status)

                _state.update { it.copy(isLoading = false, projects = projects) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error al conectar con la base de datos: ${e.message}") }
            }
        }
    }

    //Crear un nuevo projecto
    fun createProject(title: String, ownerId: String, currentStatus: String) {
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