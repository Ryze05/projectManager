package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import org.example.project.domain.models.Task

import org.example.project.network.SupabaseClient

class TaskRepository {

    suspend fun getMyTasks(): List<Task> {
        return try {
            SupabaseClient.client
                .from("tasks")
                .select {
                    // CORRECCIÓN: 'order' va directamente aquí, sin 'filter'
                    order(column = "due_date", order = Order.ASCENDING)
                }
                .decodeList<Task>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Función para marcar el Checkbox en la base de datos
    suspend fun toggleTaskCompletion(taskId: Long, currentStatus: Boolean) {
        try {
            SupabaseClient.client
                .from("tasks")
                .update(
                    {
                        set("is_completed", !currentStatus)
                    }
                ) {
                    filter {
                        eq("id", taskId)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getTasksBySection(sectionId: Long): List<Task> { // Cambiamos el parámetro
        return try {
            SupabaseClient.client
                .from("task") // Tabla 'task'
                .select {
                    filter {
                        eq("section_id", sectionId) // Filtramos por sección
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
                .decodeList<Task>()
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getAllTasksForAgenda(): List<Task> {
        return try {
            SupabaseClient.client
                .from("task") // <--- LEEMOS LA VISTA
                .select {
                    order("due_date", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
                .decodeList<Task>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}