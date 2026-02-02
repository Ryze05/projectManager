package org.example.project.ui.projectDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    fun addSection(name: String, projectId: Long, priority: String) {
        viewModelScope.launch {
            try {
                sectionRepository.createSection(name, projectId, priority)
                loadProjectContent(projectId, _state.value.projectName)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al crear sección") }
            }
        }
    }

    fun addTask(title: String, sectionId: Long, projectId: Long, priority: String, description: String?, dueDate: String?) {
        viewModelScope.launch {
            try {
                taskRepository.createTask(title, description, sectionId, priority, dueDate)
                loadProjectContent(projectId, _state.value.projectName)
            } catch (e: Exception) {
                println("❌ Error en addTask: ${e.message}")
                _state.update { it.copy(error = "Error al crear la tarea") }
            }
        }
    }
}