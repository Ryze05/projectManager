package org.example.project.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.domain.models.Project

@Composable
fun ActiveProjectsColumn(projects: List<Project>, onProjectClick: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        projects.forEach { project ->
            val colors = listOf(Color(0xFF2563EB), Color(0xFFFF8A65), Color(0xFF66BB6A), Color(0xFFAB47BC))
            val selectedColor = colors[(project.id!! % colors.size).toInt()]
            ProjectCardVertical(
                title = project.title,
                date = "Creado: ${project.createdAt?.take(10) ?: "N/A"}",
                color = selectedColor,
                onClick = { onProjectClick(project.id!!) }
            )
        }
    }
}