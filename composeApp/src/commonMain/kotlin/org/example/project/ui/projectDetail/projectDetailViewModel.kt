package org.example.project.ui.projectDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.domain.models.ProjectMember
import org.example.project.repository.AuthRepository
import org.example.project.repository.ProjectRepository
import org.example.project.repository.SectionRepository
import org.example.project.repository.TaskRepository

class ProjectDetailsViewModel(
    private val projectRepository: ProjectRepository,
    private val sectionRepository: SectionRepository,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProjectDetailsState())
    val state = _state.asStateFlow()

    fun loadProjectContent(projectId: Long, projectName: String, userId: String) {
        _state.update { it.copy(projectName = projectName, currentUserId = userId, isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val profile = authRepository.getCurrentUserProfile()
                val isAdmin = profile?.isAdmin ?: false

                val sectionsWithTasks = projectRepository.getProjectSectionsWithTasks(projectId, userId, isAdmin)
                val members = projectRepository.getProjectMembers(projectId)

                _state.update { it.copy(
                    sections = sectionsWithTasks,
                    projectMembers = members,
                    isAdmin = profile?.isAdmin ?: false,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error: ${e.message}") }
            }
        }
    }

    fun addSection(name: String, projectId: Long, priority: String) {
        if (!_state.value.isAdmin) return
        viewModelScope.launch {
            try {
                val newSection = sectionRepository.createSection(name, projectId, priority)
                _state.update { it.copy(sections = it.sections + newSection) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al crear sección") }
            }
        }
    }

    fun updateSection(sectionId: Long, name: String, priority: String) {
        val oldSections = _state.value.sections

        _state.update { state ->
            state.copy(sections = state.sections.map {
                if (it.id == sectionId) it.copy(name = name, priority = priority) else it
            })
        }

        viewModelScope.launch {
            try {
                sectionRepository.updateSection(sectionId, name, priority)
            } catch (e: Exception) {
                _state.update { it.copy(
                    sections = oldSections,
                    error = "Sincronización fallida. Reintentando..."
                ) }
            }
        }
    }

    fun deleteSection(sectionId: Long) {
        if (!_state.value.isAdmin) return

        val oldSections = _state.value.sections

        _state.update { currentState ->
            currentState.copy(
                sections = currentState.sections.filter { it.id != sectionId }
            )
        }

        viewModelScope.launch {
            try {
                sectionRepository.deleteSection(sectionId)
            } catch (e: Exception) {
                _state.update { it.copy(
                    sections = oldSections,
                    error = "No se pudo eliminar: ${e.message}"
                ) }
            }
        }
    }

    fun addTask(title: String, sectionId: Long, priority: String, description: String?, dueDate: String?) {
        if (!_state.value.isAdmin) return
        viewModelScope.launch {
            try {
                val newTask = taskRepository.createTask(title, description, sectionId, priority, dueDate)

                _state.update { currentState ->
                    currentState.copy(
                        sections = currentState.sections.map { section ->
                            if (section.id == sectionId) {
                                section.copy(task = section.task + newTask)
                            } else section
                        }
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al crear la tarea") }
            }
        }
    }

    fun updateTask(taskId: Long, title: String, description: String?, priority: String, dueDate: String?, isCompleted: Boolean) {
        if (!_state.value.isAdmin) return
        val oldSections = _state.value.sections

        _state.update { currentState ->
            currentState.copy(
                sections = currentState.sections.map { section ->
                    section.copy(task = section.task.map { task ->
                        if (task.id == taskId) {
                            task.copy(title = title, description = description, priority = priority, dueDate = dueDate, isCompleted = isCompleted)
                        } else task
                    })
                }
            )
        }

        viewModelScope.launch {
            try {
                taskRepository.updateTask(taskId, title, description, priority, dueDate, isCompleted)
            } catch (e: Exception) {
                _state.update { it.copy(sections = oldSections, error = "Error al actualizar tarea") }
            }
        }
    }

    fun deleteTask(taskId: Long) {
        if (!_state.value.isAdmin) return
        val snapshot = _state.value.sections

        _state.update { currentState ->
            currentState.copy(
                sections = currentState.sections.map { section ->
                    section.copy(task = section.task.filter { it.id != taskId })
                }
            )
        }

        viewModelScope.launch {
            try {
                taskRepository.deleteTask(taskId)
            } catch (e: Exception) {
                _state.update { it.copy(sections = snapshot, error = "Error al eliminar tarea") }
            }
        }
    }

    fun addMember(profileId: String, projectId: Long) {
        if (!_state.value.isAdmin) return

        val oldMembers = _state.value.projectMembers

        val userToAdd = _state.value.allUsers.find { it.id == profileId } ?: return

        _state.update { currentState ->
            currentState.copy(
                projectMembers = currentState.projectMembers + userToAdd
            )
        }

        viewModelScope.launch {
            try {
                val newMember = ProjectMember(profileId = profileId, projectId = projectId)
                projectRepository.addMemberToProject(newMember)
            } catch (e: Exception) {
                _state.update { it.copy(
                    projectMembers = oldMembers,
                    error = "Error al añadir miembro a la base de datos"
                ) }
            }
        }
    }

    fun toggleTaskStatus(taskId: Long, isCompleted: Boolean) {
        val snapshot = _state.value.sections

        _state.update { currentState ->
            currentState.copy(
                sections = currentState.sections.map { section ->
                    section.copy(task = section.task.map { task ->
                        if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
                    })
                }
            )
        }

        viewModelScope.launch {
            try {
                taskRepository.updateTaskCompletion(taskId, isCompleted)
            } catch (e: Exception) {
                _state.update { it.copy(sections = snapshot, error = "Error al sincronizar estado") }
            }
        }
    }

    fun loadAllProfiles() {
        viewModelScope.launch {
            try {
                val allProfiles = projectRepository.getAllProfiles()
                _state.update { it.copy(allUsers = allProfiles) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al cargar usuarios") }
            }
        }
    }

    fun removeMember(profileId: String, projectId: Long) {
        if (!_state.value.isAdmin) return

        val oldMembers = _state.value.projectMembers

        _state.update { currentState ->
            currentState.copy(
                projectMembers = currentState.projectMembers.filter { it.id != profileId }
            )
        }

        viewModelScope.launch {
            try {
                projectRepository.removeMemberFromProject(profileId, projectId)
            } catch (e: Exception) {
                _state.update { it.copy(
                    projectMembers = oldMembers,
                    error = "No se pudo eliminar al miembro"
                ) }
            }
        }
    }
}