package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import org.example.project.domain.models.Section

import org.example.project.network.SupabaseClient

class SectionRepository {

    suspend fun getSectionsByProject(projectId: Long): List<Section> {
        return try {
            SupabaseClient.client
                .from("section") // Asegúrate que coincida con tu tabla SQL 'section'
                .select {
                    filter {
                        eq("project_id", projectId)
                    }
                    order("id", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
                .decodeList<Section>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}