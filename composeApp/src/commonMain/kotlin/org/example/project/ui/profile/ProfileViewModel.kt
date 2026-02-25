package org.example.project.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.repository.AuthRepository
import org.example.project.repository.ProfileRepository

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
): ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    /*fun loadProfileData(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            println("DEBUG_PROFILE: Iniciando carga para ID: $userId")

            try {
                val profile = authRepository.getCurrentUserProfile()
                println("DEBUG_PROFILE: Objeto Perfil -> $profile")

                if (profile == null) {
                    println("DEBUG_PROFILE: ALERTA - El perfil volvió nulo. Revisa RLS en Supabase.")
                }

                val pCount = profileRepository.getProjectCount(userId)
                val tCount = profileRepository.getTaskCount(userId)
                println("DEBUG_PROFILE: Conteos -> Proyectos: $pCount, Tareas: $tCount")

                _state.update { it.copy(
                    userName = profile?.fullName ?: "Nombre no encontrado",
                    email = profile?.email ?: "Email no encontrado",
                    avatarUrl = profile?.avatarUrl,
                    isAdmin = profile?.isAdmin ?: false,
                    totalProjects = pCount,
                    totalTasks = tCount,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                println("DEBUG_PROFILE: ERROR CRÍTICO -> ${e.message}")
                e.printStackTrace()
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }*/

    fun loadProfileData(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val profile = authRepository.getCurrentUserProfile()
                val pCount = profileRepository.getProjectCount(userId)
                val tCount = profileRepository.getTaskCount(userId)

                val users = if (profile?.isAdmin == true) {
                    profileRepository.getAllProfiles().filter { it.id != userId }
                } else {
                    emptyList()
                }

                _state.update { it.copy(
                    userName = profile?.fullName ?: "Nombre no encontrado",
                    email = profile?.email ?: "Email no encontrado",
                    avatarUrl = profile?.avatarUrl,
                    isAdmin = profile?.isAdmin ?: false,
                    totalProjects = pCount,
                    totalTasks = tCount,
                    allUsers = users,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun uploadProfilePicture(userId: String, imageBytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true) }
            try {
                val newUrl = profileRepository.uploadAvatar(userId, imageBytes)

                profileRepository.updateProfile(userId, null, newUrl)

                _state.update { it.copy(
                    avatarUrl = newUrl,
                    isUploading = false
                ) }

                println("DEBUG_PROFILE: Nueva URL cargada en el estado -> $newUrl")
            } catch (e: Exception) {
                println("DEBUG_PROFILE: Error subiendo foto -> ${e.message}")
                _state.update { it.copy(isUploading = false, error = "Error al subir imagen") }
            }
        }
    }

    fun updateProfileName(userId: String, newName: String) {
        viewModelScope.launch {
            try {
                profileRepository.updateProfile(userId, newName, null)

                _state.update { it.copy(userName = newName) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al actualizar el nombre") }
            }
        }
    }

    fun toggleAdminStatus(targetUserId: String, newStatus: Boolean) {
        viewModelScope.launch {
            try {
                profileRepository.updateAdminStatus(targetUserId, newStatus)

                val updatedUsers = _state.value.allUsers.map {
                    if (it.id == targetUserId) it.copy(isAdmin = newStatus) else it
                }
                _state.update { it.copy(allUsers = updatedUsers) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "No se pudieron cambiar los permisos") }
            }
        }
    }

}