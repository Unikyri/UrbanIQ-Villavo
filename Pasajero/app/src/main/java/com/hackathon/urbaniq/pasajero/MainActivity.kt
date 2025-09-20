package com.hackathon.urbaniq.pasajero

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hackathon.urbaniq.pasajero.presentation.navigation.UrbanIQNavigation
import com.hackathon.urbaniq.pasajero.presentation.navigation.AuthNavigation
import com.hackathon.urbaniq.pasajero.presentation.viewmodel.AuthViewModel
import com.hackathon.urbaniq.pasajero.ui.theme.PasajeroTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de UrbanIQ Pasajero
 * Maneja autenticación y navegación principal
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // Launcher para permisos de ubicación
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        handleLocationPermissionResult(result)
    }
    
    // Launcher para permisos de cámara
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        handleCameraPermissionResult(result)
    }
    
    // Launcher para permisos de micrófono
    private val microphonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        handleMicrophonePermissionResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            PasajeroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Obtener AuthViewModel
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
                    
                    // Mostrar navegación de auth o app principal según estado
                    if (authState.isAuthenticated) {
                        // Usuario autenticado - mostrar app principal
                        UrbanIQNavigation()
                        
                        // Solicitar permisos necesarios después del login
                        LaunchedEffect(authState.isAuthenticated) {
                            requestLocationPermissions()
                        }
                    } else {
                        // Usuario no autenticado - mostrar pantallas de login/registro
                        AuthNavigation(
                            onAuthSuccess = {
                                // El estado se actualizará automáticamente via AuthViewModel
                            }
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Solicita permisos de ubicación
     */
    private fun requestLocationPermissions() {
        val locationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val needsPermission = locationPermissions.any { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PermissionChecker.PERMISSION_GRANTED
        }
        
        if (needsPermission) {
            locationPermissionLauncher.launch(locationPermissions)
        }
    }
    
    /**
     * Solicita permisos de cámara para QR scanner
     */
    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    /**
     * Solicita permisos de micrófono para comandos de voz
     */
    private fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PermissionChecker.PERMISSION_GRANTED) {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    /**
     * Maneja el resultado de permisos de ubicación
     */
    private fun handleLocationPermissionResult(result: Map<String, Boolean>) {
        val fineLocationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        when {
            fineLocationGranted -> {
                // Permiso de ubicación precisa otorgado
                // La app puede usar GPS
            }
            coarseLocationGranted -> {
                // Solo ubicación aproximada otorgada
                // La app puede usar ubicación de red
            }
            else -> {
                // Permisos de ubicación denegados
                // Mostrar explicación al usuario sobre por qué son necesarios
            }
        }
    }
    
    /**
     * Maneja el resultado de permisos de cámara
     */
    private fun handleCameraPermissionResult(granted: Boolean) {
        if (granted) {
            // Permiso de cámara otorgado
            // El usuario puede usar el escáner QR
        } else {
            // Permiso denegado
            // Mostrar mensaje explicativo
        }
    }
    
    /**
     * Maneja el resultado de permisos de micrófono
     */
    private fun handleMicrophonePermissionResult(granted: Boolean) {
        if (granted) {
            // Permiso de micrófono otorgado
            // El usuario puede usar comandos de voz
        } else {
            // Permiso denegado
            // Deshabilitar funciones de voz
        }
    }
}