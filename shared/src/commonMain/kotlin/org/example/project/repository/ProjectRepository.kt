package org.example.project.repository

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Columns.Companion.raw
import org.example.project.domain.models.Project
import org.example.project.domain.models.Section
import org.example.project.network.SupabaseClient

class ProjectRepository {

    suspend fun getProjectsMember(): List<Project> {
        return try {
            val res = SupabaseClient.client.from("project").select().decodeList<Project>()
            println("DEBUG_PROJECTS: Respuesta recibida: $res")
            res
        } catch (e: Exception) {
            println("DEBUG_PROJECTS: Error capturado -> ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProjectsByStatus(profileId: String, status: String): List<Project> {
        return SupabaseClient.client.from("project")
            .select(columns = Columns.raw("*, project_member!inner(profile_id)")) {
                filter {
                    eq("project_member.profile_id", profileId)
                    eq("status", status)
                }
            }
            .decodeList<Project>()
    }

    suspend fun addProject(title: String, ownerId: String) {
        val newProject = Project(title = title, ownerId = ownerId)
        SupabaseClient.client.postgrest["project"].insert(newProject)
    }

    suspend fun getProjectSectionsWithTasks(projectId: Long): List<Section> {
        return try {
            SupabaseClient.client.from("section")
                .select(columns = Columns.raw("*, task(*)")) {
                    filter {
                        eq("project_id", projectId)
                    }
                }
                .decodeList<Section>()
        } catch (e: Exception) {
            println("Error cargando secciones: ${e.message}")
            emptyList()
        }
    }
}