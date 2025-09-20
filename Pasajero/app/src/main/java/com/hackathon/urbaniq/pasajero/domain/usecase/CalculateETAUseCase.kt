package com.hackathon.urbaniq.pasajero.domain.usecase

import com.google.android.gms.maps.model.LatLng
import com.hackathon.urbaniq.pasajero.domain.model.Vehicle
import com.hackathon.urbaniq.pasajero.domain.model.MeetingPoint
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Caso de uso para calcular el ETA (tiempo estimado de llegada) de un vehículo a un punto de encuentro
 */
class CalculateETAUseCase @Inject constructor() {

    /**
     * Calcula el ETA de un vehículo al punto de encuentro
     * @param vehicle Vehículo seleccionado
     * @param meetingPoint Punto de encuentro calculado
     * @return ETA estimado en Duration
     */
    operator fun invoke(vehicle: Vehicle, meetingPoint: MeetingPoint): Duration {
        val distanceToMeetingPoint = calculateDistance(vehicle.currentLocation, meetingPoint.location)
        
        // Usar una velocidad por defecto según el tipo de vehículo
        val vehicleSpeed = getDefaultSpeedForVehicleType(vehicle.vehicleType)
        
        // Convertir velocidad de km/h a m/s
        val speedMps = (vehicleSpeed * 1000) / 3600
        
        // Calcular tiempo en segundos
        val timeInSeconds = if (speedMps > 0) {
            (distanceToMeetingPoint / speedMps).toLong()
        } else {
            0L
        }
        
        // Agregar un factor de seguridad para tráfico y paradas
        val safetyFactor = getSafetyFactorForVehicleType(vehicle.vehicleType)
        val adjustedTimeInSeconds = (timeInSeconds * safetyFactor).toLong()
        
        return adjustedTimeInSeconds.seconds
    }

    /**
     * Calcula el ETA considerando múltiples factores
     * @param vehicle Vehículo seleccionado
     * @param meetingPoint Punto de encuentro
     * @param trafficFactor Factor de tráfico (1.0 = sin tráfico, 1.5 = tráfico moderado, 2.0 = tráfico pesado)
     * @return ETA ajustado por tráfico
     */
    fun calculateWithTraffic(
        vehicle: Vehicle,
        meetingPoint: MeetingPoint,
        trafficFactor: Double = 1.2
    ): Duration {
        val baseETA = invoke(vehicle, meetingPoint)
        return (baseETA.inWholeSeconds * trafficFactor).toLong().seconds
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     */
    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val earthRadius = 6371000.0 // Radio de la Tierra en metros

        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(from.latitude)) * Math.cos(Math.toRadians(to.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    /**
     * Obtiene la velocidad por defecto según el tipo de vehículo
     */
    private fun getDefaultSpeedForVehicleType(type: com.hackathon.urbaniq.pasajero.domain.model.VehicleType): Double {
        return when (type) {
            com.hackathon.urbaniq.pasajero.domain.model.VehicleType.BUS -> 25.0 // km/h en ciudad
            com.hackathon.urbaniq.pasajero.domain.model.VehicleType.TAXI -> 35.0 // km/h en ciudad
        }
    }

    /**
     * Obtiene el factor de seguridad según el tipo de vehículo
     * Los buses tienen más paradas y demoras
     */
    private fun getSafetyFactorForVehicleType(type: com.hackathon.urbaniq.pasajero.domain.model.VehicleType): Double {
        return when (type) {
            com.hackathon.urbaniq.pasajero.domain.model.VehicleType.BUS -> 1.4 // 40% más tiempo por paradas
            com.hackathon.urbaniq.pasajero.domain.model.VehicleType.TAXI -> 1.2 // 20% más tiempo por tráfico
        }
    }

    /**
     * Formatea el ETA para mostrar al usuario
     */
    fun formatETA(eta: Duration): String {
        val minutes = eta.inWholeMinutes
        return when {
            minutes < 1 -> "Menos de 1 min"
            minutes == 1L -> "1 minuto"
            minutes < 60 -> "$minutes minutos"
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                if (remainingMinutes == 0L) {
                    "${hours}h"
                } else {
                    "${hours}h ${remainingMinutes}min"
                }
            }
        }
    }
}
