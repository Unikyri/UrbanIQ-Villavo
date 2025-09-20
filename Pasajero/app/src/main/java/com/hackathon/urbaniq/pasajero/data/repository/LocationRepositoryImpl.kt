package com.hackathon.urbaniq.pasajero.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.hackathon.urbaniq.pasajero.domain.repository.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.math.*

/**
 * Implementación del repositorio de ubicación usando Google Play Services
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override suspend fun getCurrentLocation(): Result<LatLng> {
        return try {
            if (!hasLocationPermission()) {
                return Result.failure(SecurityException("Permisos de ubicación no concedidos"))
            }

            if (!isLocationServiceEnabled()) {
                return Result.failure(IllegalStateException("Servicios de ubicación deshabilitados"))
            }

            val location = suspendCancellableCoroutine<LatLng?> { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(LatLng(location.latitude, location.longitude))
                        } else {
                            // Si no hay última ubicación conocida, solicitar una nueva
                            requestSingleLocationUpdate { newLocation ->
                                if (newLocation != null) {
                                    continuation.resume(LatLng(newLocation.latitude, newLocation.longitude))
                                } else {
                                    continuation.resume(null)
                                }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(null)
                    }
            }

            if (location != null) {
                Result.success(location)
            } else {
                Result.failure(RuntimeException("No se pudo obtener la ubicación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLocationUpdates(): Flow<LatLng> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Permisos de ubicación no concedidos"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // 5 segundos
        ).apply {
            setMinUpdateIntervalMillis(2000L) // Mínimo 2 segundos
            setMaxUpdateDelayMillis(10000L) // Máximo 10 segundos
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    trySend(LatLng(location.latitude, location.longitude))
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override suspend fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

    override suspend fun isLocationServiceEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun calculateDistance(from: LatLng, to: LatLng): Double {
        val earthRadius = 6371000.0 // Radio de la Tierra en metros

        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    override fun stopLocationUpdates() {
        // En esta implementación, el stop se maneja automáticamente por el Flow
        // cuando se cancela la coroutine que lo consume
    }

    override suspend fun startLocationUpdates(): Result<Unit> {
        return try {
            if (!hasLocationPermission()) {
                Result.failure(SecurityException("Permisos de ubicación no concedidos"))
            } else if (!isLocationServiceEnabled()) {
                Result.failure(IllegalStateException("Servicios de ubicación deshabilitados"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Solicita una actualización de ubicación única
     */
    private fun requestSingleLocationUpdate(callback: (android.location.Location?) -> Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            callback(null)
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            0L
        ).apply {
            setMaxUpdateDelayMillis(10000L)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                callback(locationResult.lastLocation)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            callback(null)
        }
    }
}
