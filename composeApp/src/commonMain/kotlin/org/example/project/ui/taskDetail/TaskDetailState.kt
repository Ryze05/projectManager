package org.example.project.ui.taskDetail

data class TaskDetailState(
    val task: org.example.project.domain.models.Task? = null,
    val projectMembers: List<org.example.project.domain.models.Profile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)