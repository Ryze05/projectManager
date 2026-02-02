package org.example.project.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Section(
    val id: Long? = null,
    val name: String,
    val priority: String = "media",
    val project_id: Long,
    val task: List<Task> = emptyList()
)