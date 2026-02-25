package org.example.project.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.domain.models.Project
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
                val profile = authRepository.getCurrentUserProfile()

                val projects = projectRepository.getProjectsWithProgress(profileId, "active")

                _state.update { it.copy(
                    userName = profile?.fullName ?: "Usuario",
                    projects = projects,
                    avatarUrl = profile?.avatarUrl,
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
                val userId = authRepository.getCurrentUserId() ?: return@launch
                val currentState = _state.value

                val sections = if (currentState.isAdmin) {
                    sectionRepository.getSectionsByProject(projectId)
                } else {
                    sectionRepository.getSectionsWithUserTasks(projectId, userId)
                }

                _state.update { it.copy(
                    isDialogLoading = false,
                    selectedProjectSections = sections
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(isDialogLoading = false, error = e.message) }
            }
        }
    }

    suspend fun getMyProjectsForChat(): List<Project> {
        return projectRepository.getMyProjects()
    }
}