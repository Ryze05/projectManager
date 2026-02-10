package org.example.project.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.project.domain.models.Project
import org.example.project.domain.models.Section

import org.example.project.repository.AuthRepository
import org.example.project.repository.ProjectRepository
import org.example.project.repository.SectionRepository

data class HomeState(
    val userName: String = "Cargando...",
    val projects: List<Project> = emptyList(),
    val selectedProjectSections: List<Section> = emptyList(), // CAMBIADO: Lista de Secciones
    val isDialogLoading: Boolean = false,
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val projectRepo = ProjectRepository()

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val name = authRepo.getCurrentUserName() ?: "Usuario"
            val projects = projectRepo.getMyProjects()

            _state.value = HomeState(
                userName = name,
                projects = projects,
                isLoading = false
            )
        }
    }
    // En HomeViewModel.kt

    // 1. Añade el repositorio de secciones
    private val sectionRepo = SectionRepository()

    // 2. Actualiza el Estado


    // 3. Actualiza la función del click
    fun onProjectClicked(projectId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDialogLoading = true, selectedProjectSections = emptyList())

            // Cargamos SECCIONES
            val sections = sectionRepo.getSectionsByProject(projectId)

            _state.value = _state.value.copy(
                isDialogLoading = false,
                selectedProjectSections = sections
            )
        }
    }
}