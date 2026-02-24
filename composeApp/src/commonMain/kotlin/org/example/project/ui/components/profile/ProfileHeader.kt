package org.example.project.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ProfileHeader(
    userName: String,
    email: String,
    avatarUrl: String?,
    isUploading: Boolean,
    onEditPhoto: () -> Unit,
    onEditName: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SECCIÓN DEL AVATAR ---
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                color = Color(0xFFDBEAFE) // Fondo azul claro por defecto
            ) {
                if (isUploading) {
                    // Estado: Subiendo imagen
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF2563EB)
                        )
                    }
                } else if (avatarUrl != null) {
                    // Estado: Tenemos imagen en Supabase
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Estado: No hay imagen (Evita el crash de ImageVector en Coil)
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(24.dp),
                        tint = Color(0xFF2563EB)
                    )
                }
            }

            // Botón flotante para la cámara
            IconButton(
                onClick = onEditPhoto,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF2563EB), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar foto",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- NOMBRE CON BOTÓN EDITAR ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 40.dp) // Offset para que el texto parezca centrado
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            IconButton(onClick = onEditName) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar nombre",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Gray
                )
            }
        }

        // --- EMAIL ---
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}