package com.hackathon.urbaniq.pasajero.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.auth.LoginScreen
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.auth.RegisterScreen
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.auth.ForgotPasswordScreen

/**
 * Navegación para las pantallas de autenticación (login, registro, etc.)
 */
@Composable
fun AuthNavigation(
    onAuthSuccess: () -> Unit
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Pantalla de login
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = onAuthSuccess
            )
        }
        
        // Pantalla de registro
        composable("register") {
            RegisterScreen(
                navController = navController,
                onRegisterSuccess = onAuthSuccess
            )
        }
        
        // Pantalla de recuperación de contraseña
        composable("forgot_password") {
            ForgotPasswordScreen(
                navController = navController
            )
        }
    }
}
