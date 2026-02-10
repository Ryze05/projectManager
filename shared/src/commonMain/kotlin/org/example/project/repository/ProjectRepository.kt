package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import org.example.project.domain.models.Project

import org.example.project.network.SupabaseClient

class ProjectRepository {

    suspend fun getMyProjects(): List<Project> {
        return try {
            // Seleccionamos de la tabla 'project' tal cual sale en tu esquema
            SupabaseClient.client
                .from("project")
                .select()
                .decodeList<Project>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}