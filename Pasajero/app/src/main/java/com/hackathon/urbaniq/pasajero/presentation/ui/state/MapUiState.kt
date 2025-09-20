package com.hackathon.urbaniq.pasajero.presentation.ui.state

import com.google.android.gms.maps.model.LatLng
import com.hackathon.urbaniq.pasajero.domain.model.Vehicle
import com.hackathon.urbaniq.pasajero.domain.model.Route
import com.hackathon.urbaniq.pasajero.domain.model.MeetingPoint
import com.hackathon.urbaniq.pasajero.domain.model.VehicleType
import kotlin.time.Duration

/**
 * Estado UI para la pantalla del mapa
 */
data class MapUiState(
    val userLocation: LatLng? = null,
    val vehicles: List<Vehicle> = emptyList(),
    val routes: List<Route> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val selectedRoute: Route? = null,
    val meetingPoint: MeetingPoint? = null,
    val eta: Duration? = null,
    val vehicleFilter: VehicleType? = null,
    val isLoading: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val error: String? = null,
    val isPaymentEnabled: Boolean = false,
    val searchRadius: Double = 2.0 // km
) {
    /**
     * Obtiene los vehículos filtrados según el filtro actual
     */
    fun getFilteredVehicles(): List<Vehicle> {
        return if (vehicleFilter != null) {
            vehicles.filter { it.type == vehicleFilter }
        } else {
            vehicles
        }
    }
    
    /**
     * Indica si hay algún error
     */
    fun hasError(): Boolean = error != null
    
    /**
     * Indica si se puede mostrar el mapa
     */
    fun canShowMap(): Boolean = userLocation != null && isLocationPermissionGranted
}
