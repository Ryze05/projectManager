package org.example.project.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.example.project.repository.AuthRepository
import org.example.project.ui.components.profile.ProfileHeader
import org.example.project.ui.components.profile.StatCard
import org.example.project.ui.components.profile.UserAdminItem

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    authRepository: AuthRepository,
    onLogout: () -> Unit,
    onPickImage: () -> Unit
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
                modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6FA)),
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
                        //StatCard("100%", "ÉXITO", Modifier.weight(1f))
                    }
                }

                if (state.isAdmin) {
                    item {
                        Text(
                            "GESTIÓN DE EQUIPO",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray,
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

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red),
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