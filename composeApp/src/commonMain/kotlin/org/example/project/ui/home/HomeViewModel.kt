package org.example.project.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.repository.AuthRepository
import org.example.project.repository.ProjectRepository
import org.example.project.repository.SectionRepository

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository,
    private val sectionRepository: SectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val profile = authRepository.getCurrentUserProfile()
                val name = profile?.fullName ?: "Usuario"

                val projects = if (profile != null) {
                    projectRepository.getProjectsWithProgress(profile.id, "active")
                } else {
                    emptyList()
                }

                _state.update { it.copy(
                    userName = name,
                    projects = projects,
                    isLoading = false,
                    isAdmin = profile?.isAdmin ?: false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Error al cargar datos de inicio: ${e.message}"
                ) }
            }
        }
    }

    fun onProjectClicked(projectId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isDialogLoading = true, selectedProjectSections = emptyList()) }
            try {
                val sections = sectionRepository.getSectionsByProject(projectId)

                _state.update { it.copy(
                    isDialogLoading = false,
                    selectedProjectSections = sections
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isDialogLoading = false,
                    error = "Error al cargar secciones: ${e.message}"
                ) }
            }
        }
    }
}