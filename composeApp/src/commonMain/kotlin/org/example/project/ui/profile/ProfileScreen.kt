package org.example.project.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.repository.AuthRepository

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    var userName by remember { mutableStateOf("Usuario") }
    var userEmail by remember { mutableStateOf("Cargando correo...") }

    // 2. EFECTO: Cargar datos al iniciar la pantalla
    LaunchedEffect(Unit) {
        val repo = AuthRepository()

        // Obtenemos el nombre (metadata)
        val name = repo.getCurrentUserName()
        if (name != null) userName = name

        // Obtenemos el email (si añadiste la función del paso 1)
        val email = repo.getCurrentUserEmail()
        if (email != null) userEmail = email
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado simple con botón configuración
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = 180f })
            Text("Perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Settings, contentDescription = "Ajustes")
        }

        // Avatar y Nombre
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
                // Botón editar flotante
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF2563EB), CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userEmail, // Antes decía "Senior Project Manager"
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Estadísticas
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatItem("42", "TAREAS", Modifier.weight(1f))
            StatItem("12", "PROYECTOS", Modifier.weight(1f))
            StatItem("98%", "ÉXITO", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("CONFIGURACIÓN DE CUENTA", color = Color.Gray, modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            ProfileOption(Icons.Default.Person, "Mi Cuenta", "Privacidad y datos personales")
            Spacer(modifier = Modifier.height(12.dp))
            ProfileOption(Icons.Default.Notifications, "Notificaciones", "Alertas de tareas y menciones")
            Spacer(modifier = Modifier.height(12.dp))
            ProfileOption(Icons.Default.Lock, "Seguridad", "Contraseña y 2FA")
            Spacer(modifier = Modifier.height(12.dp))
            ProfileOption(Icons.Default.Equalizer, "Preferencias", "Vistas por defecto e informes")
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Botón Logout
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun StatItem(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun ProfileOption(icon: ImageVector, title: String, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)).padding(8.dp)) {
                Icon(icon, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}