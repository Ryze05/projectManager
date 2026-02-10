package org.example.project.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// En Task.kt
@Serializable
data class Task(
    val id: Long,
    val title: String,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("section_id") val sectionId: Long? = null,
    // NUEVOS CAMPOS (Pueden ser nulos si la tarea no tiene proyecto asignado)
    @SerialName("project_title") val projectTitle: String? = "Sin Proyecto",
    @SerialName("project_color") val projectColor: String? = "#808080"
)