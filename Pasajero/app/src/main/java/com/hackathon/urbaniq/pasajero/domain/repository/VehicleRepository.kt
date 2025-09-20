package com.hackathon.urbaniq.pasajero.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.hackathon.urbaniq.pasajero.domain.model.Vehicle
import com.hackathon.urbaniq.pasajero.domain.model.VehicleType
import kotlinx.coroutines.flow.Flow

/**
 * Interface para el repositorio de vehículos
 */
interface VehicleRepository {
    
    /**
     * Obtiene todos los vehículos activos dentro de un radio específico
     */
    fun getVehiclesInRadius(
        center: LatLng,
        radiusKm: Double
    ): Flow<List<Vehicle>>
    
    /**
     * Obtiene vehículos filtrados por tipo
     */
    fun getVehiclesByType(type: VehicleType): Flow<List<Vehicle>>
    
    /**
     * Obtiene un vehículo específico por ID
     */
    suspend fun getVehicleById(id: String): Result<Vehicle?>
    
    /**
     * Obtiene todos los vehículos activos
     */
    fun getAllActiveVehicles(): Flow<List<Vehicle>>
    
    /**
     * Obtiene vehículos de una ruta específica
     */
    fun getVehiclesByRoute(routeId: String): Flow<List<Vehicle>>
    
    /**
     * Suscribe a actualizaciones en tiempo real de un vehículo específico
     */
    fun subscribeToVehicleUpdates(vehicleId: String): Flow<Vehicle?>
}
