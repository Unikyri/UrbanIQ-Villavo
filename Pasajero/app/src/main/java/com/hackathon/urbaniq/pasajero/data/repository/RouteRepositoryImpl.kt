package com.hackathon.urbaniq.pasajero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.hackathon.urbaniq.pasajero.core.Constants.Firebase
import com.hackathon.urbaniq.pasajero.domain.model.Route
import com.hackathon.urbaniq.pasajero.domain.repository.RouteRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Implementación temporal del repositorio de rutas usando Firebase Firestore
 */
@Singleton
class RouteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RouteRepository {

    override fun getAllActiveRoutes(): Flow<List<Route>> = callbackFlow {
        val routesRef = firestore.collection(Firebase.ROUTES_COLLECTION)
        
        val listener = routesRef
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val routes = snapshot.documents.mapNotNull { document ->
                        try {
                            document.toObject(Route::class.java)?.copy(id = document.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(routes)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getRouteById(id: String): Result<Route?> {
        return try {
            suspendCancellableCoroutine<Result<Route?>> { continuation ->
                firestore.collection(Firebase.ROUTES_COLLECTION)
                    .document(id)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            try {
                                val route = document.toObject(Route::class.java)?.copy(id = document.id)
                                continuation.resume(Result.success(route))
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchRoutes(query: String): Result<List<Route>> {
        return try {
            suspendCancellableCoroutine<Result<List<Route>>> { continuation ->
                firestore.collection(Firebase.ROUTES_COLLECTION)
                    .whereEqualTo("isActive", true)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        try {
                            val routes = snapshot.documents.mapNotNull { document ->
                                document.toObject(Route::class.java)?.copy(id = document.id)
                            }.filter { route ->
                                route.name.contains(query, ignoreCase = true) ||
                                route.description?.contains(query, ignoreCase = true) == true
                            }
                            continuation.resume(Result.success(routes))
                        } catch (e: Exception) {
                            continuation.resume(Result.failure(e))
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoutesNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<Route>> {
        // Implementación simplificada - en producción usaríamos GeoFirestore
        return try {
            suspendCancellableCoroutine<Result<List<Route>>> { continuation ->
                firestore.collection(Firebase.ROUTES_COLLECTION)
                    .whereEqualTo("isActive", true)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        try {
                            val routes = snapshot.documents.mapNotNull { document ->
                                document.toObject(Route::class.java)?.copy(id = document.id)
                            }
                            // TODO: Implementar filtrado por distancia real
                            continuation.resume(Result.success(routes))
                        } catch (e: Exception) {
                            continuation.resume(Result.failure(e))
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cacheRoutes(routes: List<Route>): Result<Unit> {
        // TODO: Implementar caché local
        return Result.success(Unit)
    }

    override suspend fun getCachedRoutes(): Result<List<Route>> {
        // TODO: Implementar lectura de caché local
        return Result.success(emptyList())
    }
}
