package org.example.project.repository

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
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

    fun isUserLoggedIn(): Boolean {
        return SupabaseClient.client.auth.currentSessionOrNull() != null
    }

}