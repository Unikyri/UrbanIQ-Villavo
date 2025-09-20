package com.hackathon.urbaniq.pasajero.domain.repository

import com.hackathon.urbaniq.pasajero.domain.model.Route
import kotlinx.coroutines.flow.Flow

/**
 * Interface para el repositorio de rutas
 */
interface RouteRepository {
    
    /**
     * Obtiene todas las rutas activas
     */
    fun getAllActiveRoutes(): Flow<List<Route>>
    
    /**
     * Obtiene una ruta específica por ID
     */
    suspend fun getRouteById(id: String): Result<Route?>
    
    /**
     * Busca rutas por nombre o descripción
     */
    suspend fun searchRoutes(query: String): Result<List<Route>>
    
    /**
     * Obtiene rutas que pasan cerca de una ubicación específica
     */
    suspend fun getRoutesNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 1.0
    ): Result<List<Route>>
    
    /**
     * Cachea las rutas localmente para acceso offline
     */
    suspend fun cacheRoutes(routes: List<Route>): Result<Unit>
    
    /**
     * Obtiene rutas desde el caché local
     */
    suspend fun getCachedRoutes(): Result<List<Route>>
}
