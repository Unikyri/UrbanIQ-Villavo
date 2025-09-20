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
import com.hackathon.urbaniq.pasajero.presentation.navigation.UrbanIQNavigation
import com.hackathon.urbaniq.pasajero.ui.theme.PasajeroTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de UrbanIQ Pasajero
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
    ) { granted ->
        handleCameraPermissionResult(granted)
    }
    
    // Launcher para permisos de micrófono
    private val microphonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        handleMicrophonePermissionResult(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Solicitar permisos esenciales al inicio
        requestLocationPermissions()
        
        setContent {
            PasajeroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UrbanIQNavigation()
                }
            }
        }
    }
    
    /**
     * Solicita permisos de ubicación si no están concedidos
     */
    private fun requestLocationPermissions() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        
        if (!fineLocationGranted || !coarseLocationGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    /**
     * Solicita permiso de cámara cuando sea necesario
     */
    fun requestCameraPermission() {
        val cameraGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PermissionChecker.PERMISSION_GRANTED
        
        if (!cameraGranted) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    /**
     * Solicita permiso de micrófono cuando sea necesario
     */
    fun requestMicrophonePermission() {
        val microphoneGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PermissionChecker.PERMISSION_GRANTED
        
        if (!microphoneGranted) {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    /**
     * Maneja el resultado de permisos de ubicación
     */
    private fun handleLocationPermissionResult(permissions: Map<String, Boolean>) {
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Permisos concedidos
        } else {
            // Permisos denegados - manejar caso
        }
    }
    
    /**
     * Maneja el resultado de permiso de cámara
     */
    private fun handleCameraPermissionResult(granted: Boolean) {
        if (granted) {
            // Permiso de cámara concedido
        } else {
            // Permiso de cámara denegado
        }
    }
    
    /**
     * Maneja el resultado de permiso de micrófono
     */
    private fun handleMicrophonePermissionResult(granted: Boolean) {
        if (granted) {
            // Permiso de micrófono concedido
        } else {
            // Permiso de micrófono denegado
        }
    }
}
