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

    init {
        println("DEBUG_PROJECTS: Init del ViewModel")
        loadProjects()
    }

    fun loadProjects() {
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
}