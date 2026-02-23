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

    fun loadHomeData(profileId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. Obtenemos el perfil completo para el nombre y el rol
                val profile = authRepository.getCurrentUserProfile()

                // 2. Obtenemos los proyectos usando el ID que nos pasan (tu estilo)
                val projects = projectRepository.getProjectsWithProgress(profileId, "active")

                _state.update { it.copy(
                    userName = profile?.fullName ?: "Usuario",
                    projects = projects,
                    isLoading = false,
                    isAdmin = profile?.isAdmin ?: false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
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
                _state.update { it.copy(isDialogLoading = false, error = e.message) }
            }
        }
    }
}