package org.example.project.ui.profile

import org.example.project.domain.models.Profile

data class ProfileState(
    val userName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val totalProjects: Int = 0,
    val totalTasks: Int = 0,
    val isAdmin: Boolean = false,
    val allUsers: List<Profile> = emptyList(),
    val isLoading: Boolean = true,
    val isUploading: Boolean = false,
    val error: String? = null
)