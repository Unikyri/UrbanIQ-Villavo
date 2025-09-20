package com.hackathon.urbaniq.pasajero.domain.repository

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

/**
 * Interface para el repositorio de ubicación
 */
interface LocationRepository {
    
    /**
     * Obtiene la ubicación actual del usuario
     */
    suspend fun getCurrentLocation(): Result<LatLng>
    
    /**
     * Suscribe a actualizaciones de ubicación en tiempo real
     */
    fun getLocationUpdates(): Flow<LatLng>
    
    /**
     * Verifica si los permisos de ubicación están concedidos
     */
    suspend fun hasLocationPermission(): Boolean
    
    /**
     * Verifica si el servicio de ubicación está habilitado
     */
    suspend fun isLocationServiceEnabled(): Boolean
    
    /**
     * Calcula la distancia entre dos puntos en metros
     */
    fun calculateDistance(from: LatLng, to: LatLng): Double
    
    /**
     * Detiene las actualizaciones de ubicación
     */
    fun stopLocationUpdates()
    
    /**
     * Inicia las actualizaciones de ubicación
     */
    suspend fun startLocationUpdates(): Result<Unit>
}
