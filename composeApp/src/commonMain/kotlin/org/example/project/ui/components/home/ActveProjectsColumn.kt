package org.example.project.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.domain.models.Project
import org.example.project.ui.projects.ProjectUiState
import org.example.project.ui.theme.projectColors

@Composable
fun ActiveProjectsColumn(projects: List<Project>, onProjectClick: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        projects.forEach { project ->
            val selectedColor = remember(project.id) {
                val base = projectColors[(project.id!! % projectColors.size).toInt()]
                base.copy(alpha = 0.2f)
            }

            ProjectCardVertical(
                title = project.title,
                date = "Creado: ${project.createdAt?.take(10) ?: "N/A"}",
                color = selectedColor,
                onClick = { onProjectClick(project.id!!) }
            )
        }
    }
}