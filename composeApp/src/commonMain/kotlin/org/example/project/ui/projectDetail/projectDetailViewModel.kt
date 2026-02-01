package org.example.project.ui.projectDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.repository.ProjectRepository

class ProjectDetailsViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProjectDetailsState())
    val state = _state.asStateFlow()

    fun loadProjectContent(projectId: Long, projectName: String) {
        _state.update { it.copy(projectName = projectName, isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val sectionsWithTasks = projectRepository.getProjectSectionsWithTasks(projectId)

                _state.update { it.copy(sections = sectionsWithTasks, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error al cargar el tablero: ${e.message}") }
            }
        }
    }
}