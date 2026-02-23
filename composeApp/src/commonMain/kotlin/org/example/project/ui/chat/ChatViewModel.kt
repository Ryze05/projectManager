package org.example.project.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.project.domain.models.Message
import org.example.project.repository.AuthRepository
import org.example.project.repository.ChatRepository

// AHORA RECIBE EL ID POR CONSTRUCTOR
class ChatViewModel(private val projectId: Long) : ViewModel() {
    private val repo = ChatRepository(projectId)
    private val authRepo = AuthRepository()

    var messages = mutableStateListOf<Message>()
        private set

    var currentUserName by mutableStateOf("Usuario")
        private set

    init {
        viewModelScope.launch {
            currentUserName = authRepo.getCurrentUserName() ?: "Usuario"

            // 1. CARGAMOS EL HISTORIAL ANTIGUO
            val history = repo.getHistoricalMessages()
            messages.addAll(history)

            // 2. ESCUCHAMOS LOS NUEVOS
            try {
                repo.listenToMessages().collect { newMessage ->
                    messages.add(newMessage)
                }
            } catch (e: Exception) {
                println("Error en el chat: ${e.message}")
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            repo.sendMessage(userName = currentUserName, content = text)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            repo.disconnect()
        }
    }
}