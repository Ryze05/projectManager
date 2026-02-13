package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import org.example.project.domain.models.Section
import org.example.project.domain.models.Task
import org.example.project.domain.models.TaskAssignment
import org.example.project.network.SupabaseClient

class TaskRepository {
    suspend fun createTask(title: String, description: String? = null, sectionId: Long, priority: String, dueDate: String? = null): Task {
        val newTask = Task(
            title = title,
            description = description,
            section_id = sectionId,
            priority = priority,
            dueDate = dueDate
        )

        return SupabaseClient.client.from("task").insert(newTask) {
            select()
        }.decodeSingle<Task>()
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

    suspend fun unassignUserFromTask(taskId: Long, profileId: String) {
        SupabaseClient.client.from("task_assignment").delete {
            filter {
                eq("task_id", taskId)
                eq("profile_id", profileId)
            }
        }
    }

    suspend fun getTaskById(taskId: Long): Task? {
        return try {
            SupabaseClient.client.from("task")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("""
                *,
                profiles:profile(*)
            """.trimIndent())) {
                    filter { eq("id", taskId) }
                }
                .decodeSingle<Task>()
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