package com.hackathon.urbaniq.pasajero.data.repository

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.hackathon.urbaniq.pasajero.domain.model.Vehicle
import com.hackathon.urbaniq.pasajero.domain.model.VehicleType
import com.hackathon.urbaniq.pasajero.domain.repository.VehicleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación mock del repositorio de vehículos para testing
 * Proporciona datos de ejemplo sin necesidad de Firebase
 */
@Singleton
class MockVehicleRepositoryImpl @Inject constructor() : VehicleRepository {

    // Datos de ejemplo para Villavicencio según estructura Firestore
    private val sampleVehicles = listOf(
        Vehicle(
            id = "bus001",
            location = LatLng(4.1533, -73.6350), // Centro de Villavicencio
            status = "active",
            type = "bus",
            plate = "ABC-123",
            driverId = "driver001",
            companyId = "company001",
            routeId = "ruta1"
        ),
        Vehicle(
            id = "bus002",
            location = LatLng(4.1580, -73.6380), // Norte de Villavicencio
            status = "active",
            type = "bus",
            plate = "DEF-456",
            driverId = "driver002",
            companyId = "company001",
            routeId = "ruta2"
        ),
        Vehicle(
            id = "taxi001",
            location = LatLng(4.1500, -73.6320), // Sur de Villavicencio
            status = "active",
            type = "taxi",
            plate = "GHI-789",
            driverId = "driver003",
            companyId = "company002",
            routeId = null
        ),
        Vehicle(
            id = "taxi002",
            location = LatLng(4.1560, -73.6300), // Este de Villavicencio
            status = "active",
            type = "taxi",
            plate = "JKL-012",
            driverId = "driver004",
            companyId = "company002",
            routeId = null
        ),
        Vehicle(
            id = "bus003",
            location = LatLng(4.1520, -73.6400), // Oeste de Villavicencio
            status = "active",
            type = "bus",
            plate = "MNO-345",
            driverId = "driver005",
            companyId = "company001",
            routeId = "ruta3"
        )
    )

    override fun getVehiclesInRadius(center: LatLng, radiusKm: Double): Flow<List<Vehicle>> = flow {
        // Simular carga
        delay(1000)
        
        // Filtrar vehículos dentro del radio
        val vehiclesInRadius = sampleVehicles.filter { vehicle ->
            val distance = calculateDistance(center, vehicle.location)
            distance <= radiusKm * 1000 // Convertir km a metros
        }
        
        emit(vehiclesInRadius)
        
        // Simular actualizaciones en tiempo real
        while (true) {
            delay(5000) // Actualizar cada 5 segundos
            // Mover vehículos ligeramente para simular movimiento
            val updatedVehicles = vehiclesInRadius.map { vehicle ->
                vehicle.copy(
                    location = LatLng(
                        vehicle.location.latitude + (Math.random() - 0.5) * 0.001,
                        vehicle.location.longitude + (Math.random() - 0.5) * 0.001
                    )
                )
            }
            emit(updatedVehicles)
        }
    }

    override fun getVehiclesByType(type: VehicleType): Flow<List<Vehicle>> = flow {
        delay(500)
        val typeString = when (type) {
            VehicleType.BUS -> "bus"
            VehicleType.TAXI -> "taxi"
        }
        val filteredVehicles = sampleVehicles.filter { it.type == typeString }
        emit(filteredVehicles)
    }

    override suspend fun getVehicleById(id: String): Result<Vehicle?> {
        delay(200)
        val vehicle = sampleVehicles.find { it.id == id }
        return Result.success(vehicle)
    }

    override fun getAllActiveVehicles(): Flow<List<Vehicle>> = flow {
        delay(800)
        emit(sampleVehicles.filter { it.status == "active" })
    }

    override fun getVehiclesByRoute(routeId: String): Flow<List<Vehicle>> = flow {
        delay(300)
        val routeVehicles = sampleVehicles.filter { it.routeId == routeId }
        emit(routeVehicles)
    }

    override fun subscribeToVehicleUpdates(vehicleId: String): Flow<Vehicle?> = flow {
        val vehicle = sampleVehicles.find { it.id == vehicleId }
        if (vehicle != null) {
            emit(vehicle)
            
            // Simular actualizaciones del vehículo específico
            while (true) {
                delay(3000)
                val updatedVehicle = vehicle.copy(
                    location = LatLng(
                        vehicle.location.latitude + (Math.random() - 0.5) * 0.0005,
                        vehicle.location.longitude + (Math.random() - 0.5) * 0.0005
                    )
                )
                emit(updatedVehicle)
            }
        } else {
            emit(null)
        }
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
}
