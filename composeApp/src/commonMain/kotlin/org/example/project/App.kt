package org.example.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.example.project.repository.AuthRepository
import org.example.project.ui.auth.AuthViewModel
import org.example.project.ui.auth.LoginScreen
import org.example.project.ui.auth.RegisterScreen
import org.example.project.ui.home.HomeScreen
import org.example.project.ui.navigation.Screen
import org.example.project.ui.theme.ProjectManagerTheme

@Composable
@Preview
fun App() {
    ProjectManagerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            val authRepository = remember { AuthRepository() }
            val viewModelAuth = remember { AuthViewModel(authRepository) }

            //TODO sesion persistente
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = viewModelAuth,
                        onNavigateToRegister = {
                            navController.navigate(Screen.Register.route)
                        },
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = viewModelAuth,
                        onBackToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen()
                }
            }
        }
    }
}