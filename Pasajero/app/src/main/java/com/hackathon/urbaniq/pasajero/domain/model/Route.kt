package com.hackathon.urbaniq.pasajero.domain.model

import com.google.android.gms.maps.model.LatLng

/**
 * Entidad que representa una ruta según estructura Firestore
 */
data class Route(
    val id: String = "",
    val name: String = "",
    val path: List<LatLng> = emptyList(), // Lista de geopoints que forman la ruta
    val fare: Double = 0.0 // Costo del pasaje para esta ruta
) {
    // Propiedades computadas para compatibilidad
    val polyline: List<LatLng> get() = path
    val color: String get() = "#2196F3" // Color azul por defecto
    val isActive: Boolean get() = path.isNotEmpty()
    val description: String? get() = "Ruta $name"
    
    /**
     * Encuentra el punto más cercano en la ruta a una ubicación dada
     */
    fun findClosestPointTo(location: LatLng): LatLng? {
        if (path.isEmpty()) return null
        
        return path.minByOrNull { point ->
            val earthRadius = 6371000.0 // Radio de la Tierra en metros
            val dLat = Math.toRadians(location.latitude - point.latitude)
            val dLng = Math.toRadians(location.longitude - point.longitude)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(point.latitude)) *
                    Math.cos(Math.toRadians(location.latitude)) *
                    Math.sin(dLng / 2) * Math.sin(dLng / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            earthRadius * c
        }
    }
}