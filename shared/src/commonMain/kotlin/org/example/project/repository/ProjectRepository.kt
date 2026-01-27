package org.example.project.repository

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns.Companion.raw
import org.example.project.domain.models.Project
import org.example.project.network.SupabaseClient

class ProjectRepository {

    suspend fun getProjectsMember(): List<Project> {
        return try {
            println("DEBUG_PROJECTS: Test de conexión simple...")
            // Primero intentamos traer TODO sin filtros raros
            val res = SupabaseClient.client.from("project").select().decodeList<Project>()
            println("DEBUG_PROJECTS: Respuesta recibida: $res")
            res
        } catch (e: Exception) {
            println("DEBUG_PROJECTS: Error capturado -> ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addProject(title: String, ownerId: String) {
        val newProject = Project(title = title, ownerId = ownerId)
        SupabaseClient.client.postgrest["project"].insert(newProject)
    }
}