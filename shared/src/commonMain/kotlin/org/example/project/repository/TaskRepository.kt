package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import org.example.project.domain.models.TaskAssignment
import org.example.project.network.SupabaseClient

class TaskRepository {
    suspend fun createTask(title: String, description: String? = null, sectionId: Long, priority: String, dueDate: String? = null) {
        val newTask = org.example.project.domain.models.Task(
            title = title,
            description = description,
            section_id = sectionId,
            priority = priority,
            dueDate = dueDate
        )
        SupabaseClient.client.from("task").insert(newTask)
    }

    suspend fun assignUserToTask(taskId: Long, profileId: String) {
        try {
            val assignment = TaskAssignment(
                taskId = taskId,
                profileId = profileId
            )

            SupabaseClient.client.from("task_assignment").insert(assignment)

        } catch (e: Exception) {
            println("ERROR REPO ASSIGN: ${e.message}")
        }
    }

    suspend fun getTaskById(taskId: Long): org.example.project.domain.models.Task? {
        return try {
            SupabaseClient.client.from("task")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("""
                *,
                profiles:profile(*)
            """.trimIndent())) {
                    filter { eq("id", taskId) }
                }
                .decodeSingle<org.example.project.domain.models.Task>()
        } catch (e: Exception) {
            println("Error obteniendo tarea individual: ${e.message}")
            null
        }
    }

    suspend fun updateTask(taskId: Long, title: String, description: String?, priority: String, dueDate: String?, isCompleted: Boolean) {
        SupabaseClient.client.from("task").update(
            {
                set("title", title)
                set("description", description)
                set("priority", priority)
                set("due_date", dueDate)
                set("is_completed", isCompleted)
            }
        ) {
            filter { eq("id", taskId) }
        }
    }

    suspend fun deleteTask(taskId: Long) {
        SupabaseClient.client.from("task").delete {
            filter { eq("id", taskId) }
        }
    }

    suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean) {
        SupabaseClient.client.from("task").update(
            {
                set("is_completed", isCompleted)
            }
        ) {
            filter { eq("id", taskId) }
        }
    }
}