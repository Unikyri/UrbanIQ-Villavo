package com.hackathon.urbaniq.pasajero.presentation.ui.screens.map

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * Pantalla de mapa simplificada para debugging
 * No usa ViewModel ni datos complejos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMapScreen() {
    Log.d("DebugMapScreen", "Iniciando DebugMapScreen")
    
    // Ubicación fija de Villavicencio
    val villavicencio = LatLng(4.1533, -73.6350)
    
    var isMapLoaded by remember { mutableStateOf(false) }
    var mapError by remember { mutableStateOf<String?>(null) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(villavicencio, 15f)
    }
    
    Log.d("DebugMapScreen", "Configuración del mapa lista")

    Box(modifier = Modifier.fillMaxSize()) {
        
        // Google Maps básico
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false, // Deshabilitado para simplificar
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = true,
                compassEnabled = true,
                rotationGesturesEnabled = true,
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = true,
                zoomGesturesEnabled = true
            ),
            onMapLoaded = {
                Log.d("DebugMapScreen", "¡Google Maps cargado exitosamente!")
                isMapLoaded = true
            }
        ) {
            // Marcador simple en el centro de Villavicencio
            Marker(
                state = MarkerState(position = villavicencio),
                title = "Villavicencio",
                snippet = "Centro de la ciudad"
            )
        }
        
        // Indicador de estado
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMapLoaded) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = when {
                    mapError != null -> "Error: $mapError"
                    isMapLoaded -> "✅ Mapa cargado correctamente"
                    else -> "🔄 Cargando mapa..."
                },
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Información de debugging
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🔧 Debug Info",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Ubicación: Villavicencio (4.1533, -73.6350)",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Zoom: 15x",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Mapa cargado: ${if (isMapLoaded) "Sí" else "No"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Permisos ubicación: No requeridos",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
