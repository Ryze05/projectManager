package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import org.example.project.domain.models.Task
import org.example.project.domain.models.Task2
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
    suspend fun getAllTasksForAgenda(): List<Task> {
        return try {
            SupabaseClient.client
                .postgrest.rpc("get_my_agenda_tasks")
                .decodeList<Task>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getTasksBySection(sectionId: Long): List<Task2> {
        return try {
            val query = "*, section(name)"

            val response = SupabaseClient.client.from("task")
                .select(columns = Columns.raw(query)) {
                    filter { eq("section_id", sectionId) }
                }

            response.decodeList<Task2>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun toggleTaskCompletion(taskId: Long?, isCompleted: Boolean) {
        updateTaskCompletion(taskId, isCompleted)
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
                .select(columns = Columns.raw("""
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

    suspend fun updateTaskCompletion(taskId: Long?, isCompleted: Boolean) {
        SupabaseClient.client.from("task").update(
            {
                set("is_completed", isCompleted)
            }
        ) {
            filter {
                if (taskId != null) {
                    eq("id", taskId)
                }
            }
        }
    }

    suspend fun getProjectSectionsWithTasksAgenda(userId: String): List<Task2> {
        return try {
            val query = """
        *,
        section(
            name,
            project(title)
        ),
        task_assignment!inner(profile_id)
    """.trimIndent()

            println("DEBUG: Iniciando consulta de agenda para el usuario: $userId")

            val response = SupabaseClient.client.from("task")
                .select(columns = Columns.raw(query)) {
                    filter {
                        eq("task_assignment.profile_id", userId)
                    }
                }

            val tasks = response.decodeList<Task2>()
            println("DEBUG: Tareas decodificadas: ${tasks.size}")

            tasks
        } catch (e: Exception) {
            println("DEBUG: ERROR en la consulta: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTasksBySectionFiltered(sectionId: Long, userId: String): List<Task2> {
        return try {
            val query = """
            *,
            section(name),
            task_assignment!inner(profile_id)
        """.trimIndent()

            val response = SupabaseClient.client.from("task")
                .select(columns = Columns.raw(query)) {
                    filter {
                        eq("section_id", sectionId)
                        eq("task_assignment.profile_id", userId)
                    }
                }

            response.decodeList<Task2>()
        } catch (e: Exception) {
            println("ERROR REPO FILTERED TASKS: ${e.message}")
            emptyList()
        }
    }
}