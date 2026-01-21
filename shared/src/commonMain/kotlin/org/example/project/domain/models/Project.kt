package org.example.project.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Int,
    val title: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("owner_id") val ownerId: String
)