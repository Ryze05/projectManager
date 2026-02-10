package org.example.project.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Section(
    val id: Long,
    val name: String,
    @SerialName("project_id") val projectId: Long? = null,
    val priority: String? = "baja" // Opcional, según tu SQL
)