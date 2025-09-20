package com.hackathon.urbaniq.pasajero.presentation.ui.screens.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hackathon.urbaniq.pasajero.domain.model.Vehicle
import com.hackathon.urbaniq.pasajero.domain.model.VehicleType
import com.hackathon.urbaniq.pasajero.presentation.viewmodel.MapViewModel
import kotlin.math.*

/**
 * Pantalla principal que muestra lista de vehículos cercanos
 * Reemplaza el mapa con una lista más funcional y confiable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header con información del usuario
        LocationHeader(
            userLocation = uiState.userLocation,
            isLoading = uiState.isLoading
        )
        
        // Filtros de vehículos
        VehicleFilters(
            selectedFilter = uiState.vehicleFilter,
            onFilterSelected = { viewModel.setVehicleFilter(it) },
            vehicleCount = uiState.vehicles.size
        )
        
        // Lista de vehículos
        if (uiState.isLoading) {
            LoadingSection()
        } else if (uiState.vehicles.isEmpty()) {
            EmptyVehiclesSection()
        } else {
            VehiclesList(
                vehicles = uiState.vehicles.filter { vehicle ->
                    uiState.vehicleFilter?.let { filter ->
                        vehicle.vehicleType == filter
                    } ?: true
                },
                userLocation = uiState.userLocation,
                onVehicleClick = { vehicle ->
                    viewModel.selectVehicle(vehicle)
                    // TODO: Navegar a pantalla de detalles o pago
                }
            )
        }
        
        // FAB para chatbot
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("chat") },
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Asistente IA",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
    
    // Mostrar errores
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: Mostrar Snackbar con error
        }
    }
}

@Composable
private fun LocationHeader(
    userLocation: com.google.android.gms.maps.model.LatLng?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "🚌 UrbanIQ - Villavicencio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Obteniendo tu ubicación...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                userLocation?.let { location ->
                    Text(
                        text = "📍 Lat: ${String.format("%.4f", location.latitude)}, Lng: ${String.format("%.4f", location.longitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } ?: run {
                    Text(
                        text = "📍 Ubicación no disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleFilters(
    selectedFilter: VehicleType?,
    onFilterSelected: (VehicleType?) -> Unit,
    vehicleCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(
            onClick = { onFilterSelected(null) },
            label = { Text("Todos ($vehicleCount)") },
            selected = selectedFilter == null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Todos los vehículos"
                )
            }
        )
        
        FilterChip(
            onClick = { onFilterSelected(VehicleType.BUS) },
            label = { Text("Buses") },
            selected = selectedFilter == VehicleType.BUS,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = "Solo buses"
                )
            }
        )
        
        FilterChip(
            onClick = { onFilterSelected(VehicleType.TAXI) },
            label = { Text("Taxis") },
            selected = selectedFilter == VehicleType.TAXI,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocalTaxi,
                    contentDescription = "Solo taxis"
                )
            }
        )
    }
}

@Composable
private fun LoadingSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Buscando vehículos cercanos...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Conectando con Firebase",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyVehiclesSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = "Sin vehículos",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay vehículos disponibles",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Intenta ampliar tu búsqueda o verifica tu conexión",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VehiclesList(
    vehicles: List<Vehicle>,
    userLocation: com.google.android.gms.maps.model.LatLng?,
    onVehicleClick: (Vehicle) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(vehicles) { vehicle ->
            VehicleCard(
                vehicle = vehicle,
                userLocation = userLocation,
                onClick = { onVehicleClick(vehicle) }
            )
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    userLocation: com.google.android.gms.maps.model.LatLng?,
    onClick: () -> Unit
) {
    val distance = userLocation?.let { userLoc ->
        calculateDistance(
            userLoc.latitude, userLoc.longitude,
            vehicle.location.latitude, vehicle.location.longitude
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de vehículo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        when (vehicle.vehicleType) {
                            VehicleType.BUS -> Color(0xFF1976D2)
                            VehicleType.TAXI -> Color(0xFFF57C00)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (vehicle.vehicleType) {
                        VehicleType.BUS -> Icons.Default.DirectionsBus
                        VehicleType.TAXI -> Icons.Default.LocalTaxi
                    },
                    contentDescription = vehicle.vehicleType.displayName,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del vehículo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${vehicle.vehicleType.displayName} - ${vehicle.plate}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                vehicle.routeId?.let { routeId ->
                    Text(
                        text = "🚏 Ruta: $routeId",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = "Estado",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Activo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Distancia y botón de acción
            Column(
                horizontalAlignment = Alignment.End
            ) {
                distance?.let { dist ->
                    Text(
                        text = "${String.format("%.1f", dist)} km",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "≈ ${(dist * 3).toInt()} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (vehicle.vehicleType) {
                            VehicleType.BUS -> Color(0xFF1976D2)
                            VehicleType.TAXI -> Color(0xFFF57C00)
                        }
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pagar",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * Calcula la distancia entre dos puntos geográficos usando la fórmula de Haversine
 */
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371 // Radio de la Tierra en km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}
