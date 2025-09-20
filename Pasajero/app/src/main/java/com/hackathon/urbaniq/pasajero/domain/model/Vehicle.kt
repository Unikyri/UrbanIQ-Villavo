package com.hackathon.urbaniq.pasajero.domain.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp

/**
 * Entidad que representa un vehículo en el sistema
 */
data class Vehicle(
    val id: String,
    val type: VehicleType,
    val licensePlate: String,
    val routeId: String?,
    val currentLocation: LatLng,
    val isActive: Boolean,
    val lastUpdated: Timestamp,
    val driverName: String? = null,
    val capacity: Int? = null,
    val estimatedSpeed: Double? = null // km/h
) {
    /**
     * Calcula la distancia en metros entre este vehículo y una ubicación dada
     */
    fun distanceTo(location: LatLng): Double {
        val earthRadius = 6371000.0 // Radio de la Tierra en metros
        val dLat = Math.toRadians(location.latitude - currentLocation.latitude)
        val dLng = Math.toRadians(location.longitude - currentLocation.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(currentLocation.latitude)) *
                Math.cos(Math.toRadians(location.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
}
