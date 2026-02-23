package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.domain.models.Message
import org.example.project.network.SupabaseClient


class ChatRepository(private val projectId: Long) { // AHORA RECIBE EL PROYECTO

    // Creamos un canal ÚNICO para este proyecto
    private val channel = SupabaseClient.client.channel("chat_project_$projectId")

    // NUEVO: Cargar mensajes antiguos
    suspend fun getHistoricalMessages(): List<Message> {
        return try {
            SupabaseClient.client.from("messages")
                .select {
                    filter { eq("project_id", projectId) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
                .decodeList<Message>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendMessage(userName: String, content: String) {
        try {
            val newMessage = Message(projectId = projectId, userName = userName, content = content)
            SupabaseClient.client.from("messages").insert(newMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun listenToMessages(): Flow<Message> {
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>("public") {
            table = "messages"

            // --- ¡AQUÍ ESTÁ LA SOLUCIÓN! ---
            // Usamos la función filter pasándole la columna, el operador y el valor
            filter("project_id", FilterOperator.EQ, projectId.toString())

        }.map { action ->
            action.decodeRecord<Message>()
        }
        channel.subscribe()
        return flow
    }

    suspend fun disconnect() {
        try {
            channel.unsubscribe()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}