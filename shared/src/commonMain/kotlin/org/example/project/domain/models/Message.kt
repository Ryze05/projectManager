package org.example.project.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Message(
    val id: Long = 0,
    @SerialName("project_id") val projectId: Long,
    @SerialName("user_name") val userName: String,
    val content: String,
    @SerialName("created_at") val createdAt: String? = null
)