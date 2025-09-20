package com.hackathon.urbaniq.pasajero.presentation.ui.screens.map

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.hackathon.urbaniq.pasajero.core.Constants.Navigation
import com.hackathon.urbaniq.pasajero.domain.model.VehicleType
import com.hackathon.urbaniq.pasajero.presentation.viewmodel.MapViewModel
import com.hackathon.urbaniq.pasajero.domain.usecase.CalculateETAUseCase

/**
 * Pantalla principal del mapa con Google Maps integrado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Configuración inicial del mapa
    val defaultLocation = LatLng(4.1533, -73.6350) // Villavicencio
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.userLocation ?: defaultLocation, 
            15f
        )
    }
    
    // Estado para verificar si Maps está listo
    var isMapLoaded by remember { mutableStateOf(false) }
    
    // Actualizar posición de cámara cuando cambie la ubicación del usuario
    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { location ->
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 15f),
                durationMs = 1000
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // Google Maps
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = uiState.isLocationPermissionGranted,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true,
                zoomControlsEnabled = false,
                compassEnabled = true,
                rotationGesturesEnabled = true,
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = true,
                zoomGesturesEnabled = true
            ),
            onMapLoaded = {
                Log.d("MapScreen", "Google Maps cargado correctamente")
                isMapLoaded = true
            }
        ) {
            // Marcadores de vehículos
            uiState.getFilteredVehicles().forEach { vehicle ->
                Marker(
                    state = MarkerState(position = vehicle.currentLocation),
                    title = "${vehicle.vehicleType.displayName} - ${vehicle.licensePlate}",
                    snippet = "Toca para más información",
                    onClick = {
                        viewModel.selectVehicle(vehicle)
                        true
                    }
                )
            }
            
            // Marcador del punto de encuentro
            uiState.meetingPoint?.let { meetingPoint ->
                Marker(
                    state = MarkerState(position = meetingPoint.location),
                    title = "Punto de Encuentro",
                    snippet = meetingPoint.description
                )
            }
        }
        
        // Filtros de vehículos
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterButton(
                text = "Todos",
                isSelected = uiState.vehicleFilter == null,
                onClick = { viewModel.filterVehiclesByType(null) }
            )
            FilterButton(
                text = "Buses",
                isSelected = uiState.vehicleFilter == VehicleType.BUS,
                onClick = { viewModel.filterVehiclesByType(VehicleType.BUS) },
                icon = Icons.Default.DirectionsBus
            )
            FilterButton(
                text = "Taxis",
                isSelected = uiState.vehicleFilter == VehicleType.TAXI,
                onClick = { viewModel.filterVehiclesByType(VehicleType.TAXI) },
                icon = Icons.Default.LocalTaxi
            )
        }
        
        // Bottom Sheet con información del vehículo seleccionado
        uiState.selectedVehicle?.let { vehicle ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "${vehicle.vehicleType.displayName} ${vehicle.licensePlate}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    uiState.meetingPoint?.let { meetingPoint ->
                        Text(
                            text = "Punto de encuentro: ${meetingPoint.getFormattedWalkingTime()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    uiState.eta?.let { eta ->
                        val etaUseCase = CalculateETAUseCase()
                        Text(
                            text = "Llegará en: ${etaUseCase.formatETA(eta)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearSelection() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        
                        Button(
                            onClick = { 
                                // TODO: Navegar a pantalla de pago
                            },
                            enabled = uiState.isPaymentEnabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pagar")
                        }
                    }
                }
            }
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
        
        // Indicador de carga del mapa y datos
        if (uiState.isLoading || !isMapLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (!isMapLoaded) "Cargando mapa..." else "Obteniendo ubicación...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Mostrar errores
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Cerrar")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    FilterChip(
        onClick = onClick,
        label = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(text)
            }
        },
        selected = isSelected
    )
}
