package org.example.project.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectMember(
    @SerialName("profile_id") val profileId: String,
    @SerialName("project_id") val projectId: Int,
    val role: String = "staff"
)