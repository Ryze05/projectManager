package org.example.project.ui.projectDetail

import org.example.project.domain.models.Profile
import org.example.project.domain.models.Section

data class ProjectDetailsState(
    val projectName: String = "",
    val sections: List<Section> = emptyList(),
    val projectMembers: List<Profile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)