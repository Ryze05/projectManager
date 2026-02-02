package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import org.example.project.domain.models.Section
import org.example.project.network.SupabaseClient

class SectionRepository {
    suspend fun createSection(name: String, projectId: Long, priority: String) {
        val newSection = Section(
            name = name,
            project_id = projectId,
            priority = priority
        )
        SupabaseClient.client.from("section").insert(newSection)
    }
}