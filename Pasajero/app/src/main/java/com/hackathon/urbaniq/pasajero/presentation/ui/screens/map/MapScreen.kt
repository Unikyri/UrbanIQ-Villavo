package com.hackathon.urbaniq.pasajero.presentation.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hackathon.urbaniq.pasajero.core.Constants.Navigation

/**
 * Pantalla principal del mapa
 * TODO: Implementar Google Maps Compose y lógica completa
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        
        // TODO: Aquí irá el componente de Google Maps
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Mapa de Vehículos",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Google Maps se implementará aquí",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Botón flotante para chat
        FloatingActionButton(
            onClick = {
                navController.navigate(Navigation.CHAT_SCREEN)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Abrir asistente"
            )
        }
        
        // TODO: Agregar filtros de vehículos
        // TODO: Agregar bottom sheet para información de vehículo
        // TODO: Implementar marcadores de vehículos en tiempo real
    }
}
