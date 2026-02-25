package org.example.project.ui.components.ProjectDetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.domain.models.Section

@Composable
fun ProjectSectionHeader(
    section: Section,
    isAdmin: Boolean,
    onAddTask: () -> Unit,
    onEditSection: () -> Unit,
    onDeleteSection: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAdmin) {
            IconButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, null, tint = Color(0xFF2563EB))
            }
        }

        Text(
            text = section.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Surface(
            color = if (section.priority == "alta") Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = section.priority.uppercase(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (section.priority == "alta") Color(0xFFB91C1C) else Color(0xFF15803D)
            )
        }

        if (isAdmin) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones de sección",
                        tint = Color.LightGray
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar sección") },
                        onClick = {
                            showMenu = false
                            onEditSection()
                        },
                        leadingIcon = { Icon(Icons.Default.AddCircle, null, Modifier.size(18.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar sección", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDeleteSection()
                        },
                        leadingIcon = { Icon(Icons.Default.RemoveCircle, null, tint = Color.Red, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }
    }
}