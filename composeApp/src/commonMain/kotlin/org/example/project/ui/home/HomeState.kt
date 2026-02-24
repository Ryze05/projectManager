package org.example.project.ui.home

import org.example.project.domain.models.Project
import org.example.project.domain.models.Section

data class HomeState(
    val userName: String = "Cargando...",
    val projects: List<Project> = emptyList(),
    val selectedProjectSections: List<Section> = emptyList(),
    val isDialogLoading: Boolean = false,
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    val avatarUrl: String? = null,
    val error: String? = null
)