package org.example.project.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Task2(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val section_id: Long,
    val priority: String = "media",
    val created_at: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    val profiles: List<Profile> = emptyList(),
    val section: SectionRelation? = null
) {
    val projectTitle: String
        get() = section?.project?.title ?: section?.name ?: "Sin Nombre"
}

@Serializable
data class SectionRelation(
    val name: String? = null,
    val project: ProjectTitleRelation? = null
)

@Serializable
data class ProjectTitleRelation(
    val title: String
)