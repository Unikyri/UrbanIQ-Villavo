package com.hackathon.urbaniq.pasajero.domain.model

import com.google.android.gms.maps.model.LatLng

/**
 * Representa un punto de encuentro óptimo entre el usuario y un vehículo
 */
data class MeetingPoint(
    val location: LatLng,
    val distanceFromUser: Double, // En metros
    val estimatedWalkingTime: Long, // En segundos
    val description: String? = null,
    val isAccessible: Boolean = true // Para usuarios con discapacidades
) {
    /**
     * Obtiene el tiempo de caminata formateado
     */
    fun getFormattedWalkingTime(): String {
        val minutes = estimatedWalkingTime / 60
        return if (minutes < 1) {
            "Menos de 1 minuto caminando"
        } else {
            "$minutes minutos caminando"
        }
    }
}
