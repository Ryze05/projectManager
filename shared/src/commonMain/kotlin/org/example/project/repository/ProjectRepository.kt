package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import org.example.project.domain.models.Profile
import org.example.project.domain.models.Project
import org.example.project.domain.models.Section
import org.example.project.network.SupabaseClient

class ProjectRepository {

    suspend fun getProjectMembers(projectId: Long): List<Profile> {
        return try {
            SupabaseClient.client.from("project_member")
                .select(columns = Columns.raw("...profile(*)")) {
                    filter {
                        eq("project_id", projectId)
                    }
                }
                .decodeList<Profile>()
        } catch (e: Exception) {
            println("Error: ${e.message}")
            emptyList()
        }
    }

    // Clase auxiliar para decodificar el anidamiento de Supabase
    @kotlinx.serialization.Serializable
    data class ProjectMemberWithProfile(
        val profile: Profile
    )

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

    /*suspend fun getProjectSectionsWithTasks(projectId: Long): List<Section> {
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
    }*/

    suspend fun getProjectSectionsWithTasks(projectId: Long): List<Section> {
        return try {
            SupabaseClient.client.from("section")
                .select(columns = Columns.raw("""
                *, 
                task(
                    *, 
                    profiles:profile(
                        *
                    )
                )
            """.trimIndent())) {
                    filter {
                        eq("project_id", projectId)
                    }
                }
                .decodeList<Section>()
        } catch (e: Exception) {
            println("Error cargando secciones con tareas y perfiles: ${e.message}")
            emptyList()
        }
    }
}