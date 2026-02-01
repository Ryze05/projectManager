package org.example.project.domain.models

import kotlinx.serialization.Serializable
@Serializable
data class Task(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val section_id: Long,
    val position: Int = 0,
    val created_at: String? = null
)