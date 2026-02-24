package org.example.project.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.repository.AuthRepository
import org.example.project.ui.components.profile.ProfileHeader
import org.example.project.ui.components.profile.StatCard
import org.example.project.ui.components.profile.UserAdminItem

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    authRepository: AuthRepository,
    onLogout: () -> Unit,
    onPickImage: () -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authRepository.getCurrentUserId()?.let { viewModel.loadProfileData(it) }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Editar Nombre") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Nombre completo") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    authRepository.getCurrentUserId()?.let { viewModel.updateProfileName(it, tempName) }
                    showEditNameDialog = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    ProfileHeader(
                        userName = state.userName,
                        email = state.email,
                        avatarUrl = state.avatarUrl,
                        isUploading = state.isUploading,
                        onEditPhoto = onPickImage,
                        onEditName = {
                            tempName = state.userName
                            showEditNameDialog = true
                        }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(state.totalTasks.toString(), "TAREAS", Modifier.weight(1f))
                        StatCard(state.totalProjects.toString(), "PROYECTOS", Modifier.weight(1f))
                    }
                }

                if (state.isAdmin) {
                    item {
                        Text(
                            text = "GESTIÓN DE EQUIPO",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // Color que se adapta al modo
                            modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
                        )
                    }
                    items(state.allUsers) { user ->
                        UserAdminItem(
                            name = user.fullName,
                            email = user.email,
                            isAdmin = user.isAdmin,
                            onToggleAdmin = { viewModel.toggleAdminStatus(user.id, it) }
                        )
                    }
                }

                // --- SECCIÓN DE AJUSTES (AHORA AL FINAL) ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "AJUSTES",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Adaptativo
                        modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp)
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Modo Oscuro",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface // Adaptativo
                            )
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onThemeToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onLogout,
                        // Para el botón de peligro, usamos el esquema de error del tema
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                    }
                }

            }
        }
    }
}