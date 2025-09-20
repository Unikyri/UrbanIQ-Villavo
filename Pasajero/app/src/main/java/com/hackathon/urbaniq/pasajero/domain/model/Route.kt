package com.hackathon.urbaniq.pasajero.domain.model

import com.google.android.gms.maps.model.LatLng

/**
 * Entidad que representa una ruta de transporte público
 */
data class Route(
    val id: String,
    val name: String,
    val description: String?,
    val polyline: List<LatLng>,
    val color: String, // Color hex para mostrar en el mapa
    val isActive: Boolean,
    val estimatedDuration: Long? = null, // Duración estimada en minutos
    val operatingHours: String? = null, // Ej: "05:00-22:00"
    val frequency: Int? = null // Frecuencia en minutos
) {
    /**
     * Encuentra el punto más cercano en la ruta a una ubicación dada
     */
    fun findClosestPointTo(location: LatLng): LatLng? {
        if (polyline.isEmpty()) return null
        
        return polyline.minByOrNull { point ->
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
