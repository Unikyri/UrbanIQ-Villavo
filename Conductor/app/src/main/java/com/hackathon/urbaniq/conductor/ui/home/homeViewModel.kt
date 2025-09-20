package com.competencia.appconductor.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Pago(val monto: Double = 0.0, val fecha: String = "")

class HomeViewModel : ViewModel() {

    // Estado de disponibilidad
    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable

    // Datos del conductor y vehículo
    private val _conductorName = MutableStateFlow("")
    val conductorName: StateFlow<String> = _conductorName

    private val _vehiculoPlaca = MutableStateFlow("")
    val vehiculoPlaca: StateFlow<String> = _vehiculoPlaca

    private val _pagos = MutableStateFlow<List<Pago>>(emptyList())
    val pagos: StateFlow<List<Pago>> = _pagos

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var vehicleId: String? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun toggleAvailability() {
        _isAvailable.value = !_isAvailable.value
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val coarseLocation = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!fineLocation && !coarseLocation) return

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Obtener vehicleId del conductor actual
        val uid = auth.currentUser?.uid ?: return
        db.collection("vehicles")
            .whereEqualTo("driverId", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents[0]
                    vehicleId = doc.id
                    _vehiculoPlaca.value = doc.getString("plate") ?: ""
                    startUpdatingLocationLoop(context)
                }
            }
    }

    private fun startUpdatingLocationLoop(context: Context) {
        viewModelScope.launch {
            while (_isAvailable.value) {
                try {
                    val fineLocation = ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    val coarseLocation = ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (fineLocation || coarseLocation) {
                        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                            location?.let { sendLocationToFirestore(it) }
                        }
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
                delay(5000)
            }
        }
    }

    private fun sendLocationToFirestore(location: Location) {
        val id = vehicleId ?: return
        val data = mapOf(
            "location" to GeoPoint(location.latitude, location.longitude),
            "status" to if (_isAvailable.value) "active" else "inactive"
        )
        db.collection("vehicles").document(id).set(data, SetOptions.merge())
    }

    // Cargar datos del conductor
    fun loadConductorAndVehicle() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                _conductorName.value = doc.getString("name") ?: ""
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Error cargando conductor: ${e.message}")
            }
    }

    // Cargar historial de pagos
    fun loadPagos() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("payments")
            .whereEqualTo("driverId", uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map { doc ->
                    Pago(
                        monto = doc.getDouble("amount") ?: 0.0,
                        fecha = doc.getString("date") ?: ""
                    )
                }
                _pagos.value = lista
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Error cargando pagos: ${e.message}")
            }
    }

    // Guardar token FCM en Firestore
    fun saveFcmToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FCM", "Token guardado en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error guardando token: ${e.message}")
            }
    }
}
