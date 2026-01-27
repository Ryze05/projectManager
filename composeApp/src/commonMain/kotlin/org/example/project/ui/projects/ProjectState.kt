package org.example.project.ui.projects

import org.example.project.domain.models.Project

data class ProjectState (
    val isLoading: Boolean = false,
    val projects: List<Project> = emptyList(),
    val error: String? = null
)