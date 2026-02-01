package org.example.project.ui.projects

import androidx.compose.ui.graphics.Color
import org.example.project.domain.models.Project

class ProjectUiState(val project: Project) {

    val title: String get() = project.title
    val status: String get() = project.status
    val displayDate: String get() = project.createdAt?.take(10) ?: "Sin fecha"

    val headerColor: Color
        get() = when ((project.id ?: 0L) % 4) {
            0L -> Color(0xFFFFF3E0)
            1L -> Color(0xFFE3F2FD)
            2L -> Color(0xFFE8F5E9)
            else -> Color(0xFFF3E5F5)
        }

    val statusColors: Pair<Color, Color>
        get() = when (project.status) {
            "completed" -> Color(0xFF2E7D32) to Color(0xFFC8E6C9)
            else -> Color(0xFF1565C0) to Color(0xFFE3F2FD)
        }
}