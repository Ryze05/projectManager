package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.repository.AuthRepository
import org.example.project.ui.auth.AuthViewModel
import org.example.project.ui.auth.RegisterScreen
import org.example.project.ui.theme.ProjectManagerTheme
import org.jetbrains.compose.resources.painterResource

import projectmanager.composeapp.generated.resources.Res
import projectmanager.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    ProjectManagerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            //TODO navhost
            val authRepository = remember { AuthRepository() }
            val viewModel = remember { AuthViewModel(authRepository) }

            RegisterScreen(
                viewModel = viewModel
            )
        }
    }
}