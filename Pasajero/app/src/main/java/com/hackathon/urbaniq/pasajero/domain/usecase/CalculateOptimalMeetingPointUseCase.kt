package com.hackathon.urbaniq.pasajero.domain.usecase

import com.google.android.gms.maps.model.LatLng
import com.hackathon.urbaniq.pasajero.domain.model.MeetingPoint
import com.hackathon.urbaniq.pasajero.domain.model.Vehicle
import com.hackathon.urbaniq.pasajero.domain.repository.RouteRepository
import javax.inject.Inject

/**
 * Caso de uso para calcular el punto de encuentro óptimo entre el usuario y un vehículo
 */
class CalculateOptimalMeetingPointUseCase @Inject constructor(
    private val routeRepository: RouteRepository
) {
    /**
     * Calcula el punto de encuentro óptimo
     * @param userLocation Ubicación actual del usuario
     * @param vehicle Vehículo seleccionado
     * @return Punto de encuentro óptimo o null si no se puede calcular
     */
    suspend operator fun invoke(
        userLocation: LatLng,
        vehicle: Vehicle
    ): Result<MeetingPoint?> {
        return try {
            // Si el vehículo no tiene ruta asignada, usar su ubicación actual
            if (vehicle.routeId == null) {
                val distance = calculateDistance(userLocation, vehicle.currentLocation)
                val walkingTime = estimateWalkingTime(distance)
                
                Result.success(
                    MeetingPoint(
                        location = vehicle.currentLocation,
                        distanceFromUser = distance,
                        estimatedWalkingTime = walkingTime,
                        description = "Ubicación actual del vehículo",
                        isAccessible = true
                    )
                )
            } else {
                // Obtener la ruta del vehículo
                val routeResult = routeRepository.getRouteById(vehicle.routeId)
                
                routeResult.fold(
                    onSuccess = { route ->
                        if (route != null && route.polyline.isNotEmpty()) {
                            // Encontrar el punto más cercano en la ruta
                            val closestPoint = findClosestPointOnRoute(userLocation, route.polyline)
                            val distance = calculateDistance(userLocation, closestPoint)
                            val walkingTime = estimateWalkingTime(distance)
                            
                            Result.success(
                                MeetingPoint(
                                    location = closestPoint,
                                    distanceFromUser = distance,
                                    estimatedWalkingTime = walkingTime,
                                    description = "Punto óptimo en la ruta ${route.name}",
                                    isAccessible = true
                                )
                            )
                        } else {
                            // Fallback a la ubicación actual del vehículo
                            val distance = calculateDistance(userLocation, vehicle.currentLocation)
                            val walkingTime = estimateWalkingTime(distance)
                            
                            Result.success(
                                MeetingPoint(
                                    location = vehicle.currentLocation,
                                    distanceFromUser = distance,
                                    estimatedWalkingTime = walkingTime,
                                    description = "Ubicación actual del vehículo",
                                    isAccessible = true
                                )
                            )
                        }
                    },
                    onFailure = { exception ->
                        Result.failure(exception)
                    }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Encuentra el punto más cercano en una ruta (polyline) a la ubicación del usuario
     */
    private fun findClosestPointOnRoute(userLocation: LatLng, polyline: List<LatLng>): LatLng {
        return polyline.minByOrNull { point ->
            calculateDistance(userLocation, point)
        } ?: polyline.first()
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
     * Estima el tiempo de caminata basado en la distancia
     * Asume una velocidad promedio de caminata de 5 km/h (1.39 m/s)
     */
    private fun estimateWalkingTime(distanceInMeters: Double): Long {
        val walkingSpeedMps = 1.39 // metros por segundo (5 km/h)
        return (distanceInMeters / walkingSpeedMps).toLong()
    }
}
