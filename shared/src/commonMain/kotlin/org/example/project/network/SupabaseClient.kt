package org.example.project.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import org.example.project.shared.BuildKonfig

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://oewefwftpmemkpikoaoy.supabase.co",
        supabaseKey = "sb_publishable_mGr3kSniLoso5estX-sVVA_qGxlou0j"
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}