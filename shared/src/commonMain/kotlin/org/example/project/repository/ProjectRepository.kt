package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import org.example.project.domain.models.Profile
import org.example.project.domain.models.Project
import org.example.project.domain.models.ProjectMember
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

    suspend fun getProjectSectionsWithTasks(projectId: Long, userId: String, isAdmin: Boolean): List<Section> {
        return try {
            val query = if (isAdmin) {
                """
                *, 
                task(
                    *, 
                    profiles:profile(*)
                )
            """.trimIndent()
            } else {
                """
                *, 
                task!inner(
                    *, 
                    task_assignment!inner(profile_id),
                    profiles:profile(*)
                )
            """.trimIndent()
            }

            SupabaseClient.client.from("section")
                .select(columns = Columns.raw(query)) {
                    filter {
                        eq("project_id", projectId)
                        if (!isAdmin) {
                            eq("task.task_assignment.profile_id", userId)
                        }
                    }
                }
                .decodeList<Section>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllProfiles(): List<Profile> {
        return try {
            SupabaseClient.client.from("profile").select().decodeList<Profile>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addMemberToProject(member: ProjectMember) {
        SupabaseClient.client.from("project_member").insert(member)
    }

    suspend fun updateProject(projectId: Long, newTitle: String, newStatus: String) {
        SupabaseClient.client.from("project").update(
            {
                set("title", newTitle)
                set("status", newStatus)
            }
        ) {
            filter { eq("id", projectId) }
        }
    }

    suspend fun deleteProject(projectId: Long) {
        SupabaseClient.client.from("project").delete {
            filter { eq("id", projectId) }
        }
    }

    suspend fun getProjectsWithProgress(profileId: String, status: String): List<Project> {
        return try {
            SupabaseClient.client.from("project_with_progress")
                .select(columns = Columns.raw("*, project_member!inner(profile_id)")) {
                    filter {
                        eq("project_member.profile_id", profileId)
                        eq("status", status)
                    }
                }
                .decodeList<Project>()
        } catch (e: Exception) {
            println("ERROR_VISTA: ${e.message}")
            emptyList()
        }
    }
    suspend fun getMyProjects(): List<Project> {
        return try {
            SupabaseClient.client
                .from("project") // Asegúrate de que tu tabla en Supabase se llama "project"
                .select()
                .decodeList<Project>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun removeMemberFromProject(profileId: String, projectId: Long) {
        SupabaseClient.client.from("project_member").delete {
            filter {
                eq("profile_id", profileId)
                eq("project_id", projectId)
            }
        }
    }
}