package com.hackathon.urbaniq.pasajero.domain.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp

/**
 * Entidad que representa un vehículo según estructura Firestore
 */
data class Vehicle(
    val id: String = "",
    val location: LatLng = LatLng(0.0, 0.0),
    val status: String = "inactive", // "active" o "inactive"
    val type: String = "bus", // "bus" o "taxi"
    val plate: String = "",
    val driverId: String = "",
    val companyId: String = "",
    val routeId: String? = null // Solo para buses
) {
    // Propiedades computadas para compatibilidad
    val currentLocation: LatLng get() = location
    val licensePlate: String get() = plate
    val isActive: Boolean get() = status == "active"
    val vehicleType: VehicleType get() = when (type) {
        "bus" -> VehicleType.BUS
        "taxi" -> VehicleType.TAXI
        else -> VehicleType.BUS
    }
    val lastUpdated: Timestamp get() = Timestamp.now()
    
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