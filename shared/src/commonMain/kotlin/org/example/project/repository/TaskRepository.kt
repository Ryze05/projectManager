package org.example.project.repository

import io.github.jan.supabase.postgrest.from
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
}