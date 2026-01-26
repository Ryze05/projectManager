package org.example.project.repository

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.example.project.network.SupabaseClient

class AuthRepository() {

    suspend fun signUp(email: String, password: String, fullName: String): Result<Unit> {
        return runCatching {
            SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                }
            }
        }
    }

    suspend fun signIn(email: String, pass: String): Result<Unit> {
        return runCatching {
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
        }
    }

    suspend fun signOut() {
        runCatching {
            SupabaseClient.client.auth.signOut()
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        SupabaseClient.client.auth.awaitInitialization()

        val user = SupabaseClient.client.auth.currentUserOrNull()

        return user != null
    }
    fun getCurrentUserName(): String? {
        val user = SupabaseClient.client.auth.currentUserOrNull()
        // Accedemos a los metadatos que guardaste como "full_name"
        return user?.userMetadata?.get("full_name")?.jsonPrimitive?.content
    }

    fun getCurrentUserEmail(): String? {
        return SupabaseClient.client.auth.currentUserOrNull()?.email
    }
}