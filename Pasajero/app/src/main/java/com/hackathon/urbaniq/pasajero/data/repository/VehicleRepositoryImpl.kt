package com.hackathon.urbaniq.pasajero.data.repository

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.hackathon.urbaniq.pasajero.core.Constants.Firebase
import com.hackathon.urbaniq.pasajero.domain.model.Vehicle
import com.hackathon.urbaniq.pasajero.domain.model.VehicleType
import com.hackathon.urbaniq.pasajero.domain.repository.VehicleRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Implementación del repositorio de vehículos usando Firebase Firestore
 * Actualizado para usar la estructura correcta de Firestore
 */
@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : VehicleRepository {

    override fun getVehiclesInRadius(center: LatLng, radiusKm: Double): Flow<List<Vehicle>> = callbackFlow {
        val vehiclesRef = firestore.collection(Firebase.VEHICLES_COLLECTION)
        
        val listener = vehiclesRef
            .whereEqualTo("status", "active") // Usar nueva estructura
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val vehicles = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Vehicle::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                // Filtrar por radio de distancia
                val vehiclesInRadius = vehicles.filter { vehicle ->
                    calculateDistance(center, vehicle.location) <= radiusKm * 1000
                }
                
                trySend(vehiclesInRadius)
            }
        
        awaitClose { listener.remove() }
    }

    override fun getVehiclesByType(type: VehicleType): Flow<List<Vehicle>> = callbackFlow {
        val typeString = when (type) {
            VehicleType.BUS -> "bus"
            VehicleType.TAXI -> "taxi"
        }
        
        val vehiclesRef = firestore.collection(Firebase.VEHICLES_COLLECTION)
        
        val listener = vehiclesRef
            .whereEqualTo("status", "active")
            .whereEqualTo("type", typeString)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val vehicles = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Vehicle::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(vehicles)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getVehicleById(id: String): Result<Vehicle?> = 
        suspendCancellableCoroutine { continuation ->
            firestore.collection(Firebase.VEHICLES_COLLECTION)
                .document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        try {
                            val vehicle = document.toObject(Vehicle::class.java)?.copy(id = document.id)
                            continuation.resume(Result.success(vehicle))
                        } catch (e: Exception) {
                            continuation.resume(Result.failure(e))
                        }
                    } else {
                        continuation.resume(Result.success(null))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }

    override fun getAllActiveVehicles(): Flow<List<Vehicle>> = callbackFlow {
        val vehiclesRef = firestore.collection(Firebase.VEHICLES_COLLECTION)
        
        val listener = vehiclesRef
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val vehicles = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Vehicle::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(vehicles)
            }
        
        awaitClose { listener.remove() }
    }

    override fun getVehiclesByRoute(routeId: String): Flow<List<Vehicle>> = callbackFlow {
        val vehiclesRef = firestore.collection(Firebase.VEHICLES_COLLECTION)
        
        val listener = vehiclesRef
            .whereEqualTo("status", "active")
            .whereEqualTo("routeId", routeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val vehicles = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Vehicle::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(vehicles)
            }
        
        awaitClose { listener.remove() }
    }

    override fun subscribeToVehicleUpdates(vehicleId: String): Flow<Vehicle?> = callbackFlow {
        val vehicleRef = firestore.collection(Firebase.VEHICLES_COLLECTION).document(vehicleId)
        
        val listener = vehicleRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            if (snapshot != null && snapshot.exists()) {
                try {
                    val vehicle = snapshot.toObject(Vehicle::class.java)?.copy(id = snapshot.id)
                    trySend(vehicle)
                } catch (e: Exception) {
                    trySend(null)
                }
            } else {
                trySend(null)
            }
        }
        
        awaitClose { listener.remove() }
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