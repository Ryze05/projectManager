package org.example.project.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val status: String = "active",
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,

    @SerialName("total_tasks") val totalTasks: Int = 0,
    @SerialName("completed_tasks") val completedTasks: Int = 0
)