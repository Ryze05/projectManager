package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import org.example.project.domain.models.Section
import org.example.project.network.SupabaseClient

class SectionRepository {
    suspend fun createSection(name: String, projectId: Long, priority: String): Section {
        val sectionToInsert = Section(
            name = name,
            project_id = projectId,
            priority = priority
        )

        return SupabaseClient.client.from("section").insert(sectionToInsert) {
            select()
        }.decodeSingle<Section>()
    }

    suspend fun updateSection(sectionId: Long, name: String, priority: String) {
        SupabaseClient.client.from("section").update(
            {
                set("name", name)
                set("priority", priority)
            }
        ) {
            filter { eq("id", sectionId) }
        }
    }

    suspend fun deleteSection(sectionId: Long) {
        SupabaseClient.client.from("section").delete {
            filter { eq("id", sectionId) }
        }
    }

    suspend fun getSectionsByProject(projectId: Long): List<Section> {
        return SupabaseClient.client.from("section").select {
            filter { eq("project_id", projectId) }
        }.decodeList<Section>()
    }

    suspend fun getSectionsWithUserTasks(projectId: Long, userId: String): List<Section> {
        return try {
            val query = """
            *, 
            task!inner(
                *, 
                task_assignment!inner(profile_id)
            )
        """.trimIndent()

            SupabaseClient.client.from("section")
                .select(columns = Columns.raw(query)) {
                    filter {
                        eq("project_id", projectId)
                        eq("task.task_assignment.profile_id", userId)
                    }
                }
                .decodeList<Section>()
        } catch (e: Exception) {
            emptyList()
        }
    }
}