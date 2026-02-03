package org.example.project.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskAssignment(
    @SerialName("profile_id") val profileId: String,
    @SerialName("task_id") val taskId: Long
)