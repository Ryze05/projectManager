package org.example.project.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.JsonObject
import org.example.project.domain.models.Profile
import org.example.project.network.SupabaseClient

class ProfileRepository {
    private val bucket = SupabaseClient.client.storage.from("avatars")

    suspend fun uploadAvatar(userId: String, byteArray: ByteArray): String {
        return try {
            val fileName = "$userId/$userId.jpg"

            println("DEBUG_STORAGE: Subiendo a la carpeta del usuario: $fileName")

            bucket.upload(path = fileName, data = byteArray) {
                upsert = true
            }

            val url = bucket.publicUrl(fileName)
            println("DEBUG_STORAGE: ¡Subida exitosa! URL: $url")
            url
        } catch (e: Exception) {
            println("DEBUG_STORAGE: ERROR -> ${e.message}")
            ""
        }
    }

    suspend fun updateProfile(userId: String, fullName: String?, avatarUrl: String?) {
        try {
            println("DEBUG_PROFILE: Intentando actualizar ID $userId")

            SupabaseClient.client.from("profile").update(
                {
                    if (fullName != null) set("full_name", fullName)
                    if (avatarUrl != null) set("avatar_url", avatarUrl)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
            println("DEBUG_PROFILE: Petición de actualización enviada correctamente")
        } catch (e: Exception) {
            println("DEBUG_PROFILE: Error en updateProfile -> ${e.message}")
            throw e
        }
    }

    suspend fun getProjectCount(userId: String): Int {
        return try {
            val response = SupabaseClient.client.from("project_member")
                .select(columns = Columns.raw("project_id")) {
                    filter {
                        eq("profile_id", userId)
                    }
                }.decodeList<JsonObject>()

            response.size
        } catch (e: Exception) {
            println("Error en getProjectCount: ${e.message}")
            0
        }
    }

    suspend fun getTaskCount(userId: String): Int {
        return try {
            val res = SupabaseClient.client.from("task_assignment")
                .select(columns = Columns.raw("task_id")) {
                    filter { eq("profile_id", userId) }
                }.decodeList<JsonObject>()
            res.size
        } catch (e: Exception) {
            println("Error en getTaskCount: ${e.message}")
            0
        }
    }

    suspend fun getAllProfiles(): List<Profile> {
        return SupabaseClient.client.from("profile").select().decodeList<Profile>()
    }

    suspend fun updateAdminStatus(targetUserId: String, status: Boolean) {
        SupabaseClient.client.from("profile").update(
            {
                set("is_admin", status)
            }
        ) {
            filter { eq("id", targetUserId) }
        }
    }
}