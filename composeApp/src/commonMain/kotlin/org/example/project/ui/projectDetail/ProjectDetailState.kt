package org.example.project.ui.projectDetail

import org.example.project.domain.models.Section

data class ProjectDetailsState(
    val isLoading: Boolean = false,
    val projectName: String = "",
    val sections: List<Section> = emptyList(),
    val error: String? = null
)