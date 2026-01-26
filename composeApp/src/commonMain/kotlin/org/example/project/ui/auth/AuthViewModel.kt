package org.example.project.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.repository.AuthRepository

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signIn(email, password)

            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message ?: "Login error") }
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signUp(email, password, fullName)

            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message ?: "Register error") }
            }
        }
    }

    fun clearState() {
        _state.update { AuthState() }
    }
}