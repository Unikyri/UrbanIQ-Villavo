package com.hackathon.urbaniq.pasajero.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.hackathon.urbaniq.pasajero.domain.model.Route

/**
 * Representa una recomendación de ruta de Gemini AI
 */
data class RouteRecommendation(
    val message: String,
    val suggestedRoutes: List<Route>,
    val hasDirectRoute: Boolean,
    val requiresTransfer: Boolean,
    val estimatedTotalTime: Long? = null, // En minutos
    val instructions: List<String> = emptyList()
)

/**
 * Interface para el repositorio de Gemini AI
 */
interface GeminiRepository {
    
    /**
     * Obtiene recomendaciones de ruta basadas en origen y destino
     */
    suspend fun getRouteRecommendation(
        origin: LatLng,
        destination: LatLng,
        availableRoutes: List<Route>
    ): Result<RouteRecommendation>
    
    /**
     * Procesa una consulta en lenguaje natural del usuario
     */
    suspend fun processNaturalLanguageQuery(
        query: String,
        userLocation: LatLng?,
        availableRoutes: List<Route>
    ): Result<String>
    
    /**
     * Obtiene sugerencias de destinos populares
     */
    suspend fun getPopularDestinations(
        userLocation: LatLng
    ): Result<List<String>>
    
    /**
     * Analiza y extrae información de ubicación de texto
     */
    suspend fun extractLocationFromText(
        text: String
    ): Result<LatLng?>
}
