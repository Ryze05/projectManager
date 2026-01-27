package org.example.project.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Long? = null,
    val title: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
)