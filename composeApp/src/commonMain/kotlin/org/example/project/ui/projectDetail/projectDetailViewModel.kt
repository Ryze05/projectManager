package org.example.project.ui.projectDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.domain.models.ProjectMember
import org.example.project.repository.ProjectRepository
import org.example.project.repository.SectionRepository
import org.example.project.repository.TaskRepository

class ProjectDetailsViewModel(
    private val projectRepository: ProjectRepository,
    private val sectionRepository: SectionRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProjectDetailsState())
    val state = _state.asStateFlow()

    fun loadProjectContent(projectId: Long, projectName: String, userId: String) {
        _state.update { it.copy(projectName = projectName, currentUserId = userId, isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val sectionsWithTasks = projectRepository.getProjectSectionsWithTasks(projectId, userId)
                val members = projectRepository.getProjectMembers(projectId)
                _state.update { it.copy(sections = sectionsWithTasks, projectMembers = members, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error: ${e.message}") }
            }
        }
    }

    fun addSection(name: String, projectId: Long, priority: String) {
        viewModelScope.launch {
            try {
                sectionRepository.createSection(name, projectId, priority)
                loadProjectContent(projectId, _state.value.projectName, _state.value.currentUserId)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al crear sección") }
            }
        }
    }

    fun addTask(title: String, sectionId: Long, projectId: Long, priority: String, description: String?, dueDate: String?) {
        viewModelScope.launch {
            try {
                taskRepository.createTask(title, description, sectionId, priority, dueDate)
                loadProjectContent(projectId, _state.value.projectName, _state.value.currentUserId)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al crear la tarea") }
            }
        }
    }

    fun addMember(profileId: String, projectId: Long) {
        viewModelScope.launch {
            try {
                val newMember = ProjectMember(profileId = profileId, projectId = projectId)
                projectRepository.addMemberToProject(newMember)
                loadProjectContent(projectId, _state.value.projectName, _state.value.currentUserId)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al añadir miembro") }
            }
        }
    }

    fun loadAvailableUsers() {
        viewModelScope.launch {
            try {
                val allProfiles = projectRepository.getAllProfiles()

                val currentMemberIds = _state.value.projectMembers.map { it.id }

                val available = allProfiles.filter { it.id !in currentMemberIds }

                _state.update { it.copy(allUsers = available) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al cargar usuarios disponibles") }
            }
        }
    }
}