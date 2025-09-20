package com.hackathon.urbaniq.pasajero.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.hackathon.urbaniq.pasajero.core.Constants
import com.hackathon.urbaniq.pasajero.domain.model.Vehicle
import com.hackathon.urbaniq.pasajero.domain.model.VehicleType
import com.hackathon.urbaniq.pasajero.domain.repository.LocationRepository
import com.hackathon.urbaniq.pasajero.domain.repository.VehicleRepository
import com.hackathon.urbaniq.pasajero.domain.usecase.CalculateOptimalMeetingPointUseCase
import com.hackathon.urbaniq.pasajero.domain.usecase.CalculateETAUseCase
import com.hackathon.urbaniq.pasajero.presentation.ui.state.MapUiState
import com.hackathon.urbaniq.pasajero.presentation.ui.state.UiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla del mapa
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val vehicleRepository: VehicleRepository,
    private val calculateOptimalMeetingPointUseCase: CalculateOptimalMeetingPointUseCase,
    private val calculateETAUseCase: CalculateETAUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var locationUpdatesJob: Job? = null
    private var vehicleUpdatesJob: Job? = null
    private var etaUpdateJob: Job? = null

    init {
        Log.d("MapViewModel", "Inicializando MapViewModel")
        checkLocationPermission()
        startLocationUpdates()
        startVehicleUpdates()
    }

    /**
     * Verifica si los permisos de ubicación están concedidos
     */
    private fun checkLocationPermission() {
        viewModelScope.launch {
            Log.d("MapViewModel", "Verificando permisos de ubicación")
            val hasPermission = locationRepository.hasLocationPermission()
            Log.d("MapViewModel", "Permisos de ubicación: $hasPermission")
            _uiState.update { it.copy(isLocationPermissionGranted = hasPermission) }
            
            if (!hasPermission) {
                Log.w("MapViewModel", "Permisos de ubicación denegados")
                _uiState.update { 
                    it.copy(error = UiError.LocationPermissionDenied.toUserFriendlyMessage()) 
                }
            }
        }
    }

    /**
     * Inicia las actualizaciones de ubicación del usuario
     */
    fun startLocationUpdates() {
        Log.d("MapViewModel", "Iniciando actualizaciones de ubicación")
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                Log.d("MapViewModel", "Obteniendo ubicación inicial")
                // Obtener ubicación inicial
                locationRepository.getCurrentLocation().fold(
                    onSuccess = { location ->
                        Log.d("MapViewModel", "Ubicación obtenida: $location")
                        _uiState.update { 
                            it.copy(
                                userLocation = location,
                                isLoading = false
                            ) 
                        }
                    },
                    onFailure = { exception ->
                        Log.e("MapViewModel", "Error obteniendo ubicación", exception)
                        handleLocationError(exception)
                    }
                )

                // Suscribirse a actualizaciones de ubicación
                locationRepository.getLocationUpdates()
                    .catch { exception ->
                        handleLocationError(exception)
                    }
                    .collect { location ->
                        _uiState.update { it.copy(userLocation = location) }
                        
                        // Recalcular ETA si hay un vehículo seleccionado
                        val currentState = _uiState.value
                        if (currentState.selectedVehicle != null && currentState.meetingPoint != null) {
                            calculateETA(currentState.selectedVehicle, currentState.meetingPoint)
                        }
                    }
            } catch (e: Exception) {
                handleLocationError(e)
            }
        }
    }

    /**
     * Inicia las actualizaciones de vehículos
     */
    private fun startVehicleUpdates() {
        Log.d("MapViewModel", "Iniciando actualizaciones de vehículos")
        vehicleUpdatesJob?.cancel()
        vehicleUpdatesJob = viewModelScope.launch {
            _uiState.value.userLocation?.let { userLocation ->
                try {
                    Log.d("MapViewModel", "Cargando vehículos cerca de: $userLocation")
                    val currentFilter = _uiState.value.vehicleFilter
                    val vehicleFlow = if (currentFilter != null) {
                        vehicleRepository.getVehiclesByType(currentFilter)
                    } else {
                        vehicleRepository.getVehiclesInRadius(
                            userLocation, 
                            Constants.VEHICLE_SEARCH_RADIUS_KM
                        )
                    }

                    vehicleFlow
                        .catch { exception ->
                            _uiState.update { 
                                it.copy(error = "Error al cargar vehículos: ${exception.message}") 
                            }
                        }
                        .collect { vehicles ->
                            Log.d("MapViewModel", "Vehículos cargados: ${vehicles.size}")
                            _uiState.update { it.copy(vehicles = vehicles) }
                        }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(error = "Error al conectar con el servidor: ${e.message}") 
                    }
                }
            }
        }
    }

    /**
     * Selecciona un vehículo y calcula el punto de encuentro
     */
    fun selectVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                _uiState.update { 
                    it.copy(
                        selectedVehicle = vehicle,
                        isLoading = true,
                        error = null
                    ) 
                }

                val userLocation = _uiState.value.userLocation
                if (userLocation != null) {
                    // Calcular punto de encuentro óptimo
                    calculateOptimalMeetingPointUseCase(userLocation, vehicle).fold(
                        onSuccess = { meetingPoint ->
                            _uiState.update { 
                                it.copy(
                                    meetingPoint = meetingPoint,
                                    isLoading = false
                                ) 
                            }
                            
                            // Calcular ETA
                            if (meetingPoint != null) {
                                calculateETA(vehicle, meetingPoint)
                                checkPaymentProximity(userLocation, vehicle)
                            }
                        },
                        onFailure = { exception ->
                            _uiState.update { 
                                it.copy(
                                    error = "Error al calcular punto de encuentro: ${exception.message}",
                                    isLoading = false
                                ) 
                            }
                        }
                    )
                } else {
                    _uiState.update { 
                        it.copy(
                            error = "Ubicación del usuario no disponible",
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
            }
        }
    }

    /**
     * Calcula el ETA del vehículo al punto de encuentro
     */
    private fun calculateETA(vehicle: Vehicle, meetingPoint: com.hackathon.urbaniq.pasajero.domain.model.MeetingPoint) {
        etaUpdateJob?.cancel()
        etaUpdateJob = viewModelScope.launch {
            try {
                val eta = calculateETAUseCase(vehicle, meetingPoint)
                _uiState.update { it.copy(eta = eta) }
                
                // Programar recálculo periódico del ETA
                kotlinx.coroutines.delay(Constants.ETA_UPDATE_INTERVAL_MS)
                if (_uiState.value.selectedVehicle?.id == vehicle.id) {
                    calculateETA(vehicle, meetingPoint)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Error al calcular ETA: ${e.message}") 
                }
            }
        }
    }

    /**
     * Verifica si el usuario está lo suficientemente cerca para habilitar el pago
     */
    private fun checkPaymentProximity(userLocation: LatLng, vehicle: Vehicle) {
        val distance = locationRepository.calculateDistance(userLocation, vehicle.currentLocation)
        val isPaymentEnabled = distance <= Constants.PAYMENT_PROXIMITY_THRESHOLD_METERS
        
        _uiState.update { it.copy(isPaymentEnabled = isPaymentEnabled) }
    }

    /**
     * Aplica filtro por tipo de vehículo
     */
    fun filterVehiclesByType(type: VehicleType?) {
        _uiState.update { 
            it.copy(
                vehicleFilter = type,
                selectedVehicle = null,
                meetingPoint = null,
                eta = null,
                isPaymentEnabled = false
            ) 
        }
        startVehicleUpdates()
    }

    /**
     * Limpia la selección actual
     */
    fun clearSelection() {
        etaUpdateJob?.cancel()
        _uiState.update { 
            it.copy(
                selectedVehicle = null,
                meetingPoint = null,
                eta = null,
                isPaymentEnabled = false
            ) 
        }
    }

    /**
     * Maneja errores de ubicación
     */
    private fun handleLocationError(exception: Throwable) {
        val error = when (exception) {
            is SecurityException -> UiError.LocationPermissionDenied
            is IllegalStateException -> UiError.LocationServiceDisabled
            else -> UiError.LocationUnavailable
        }
        
        _uiState.update { 
            it.copy(
                error = error.toUserFriendlyMessage(),
                isLoading = false
            ) 
        }
    }

    /**
     * Limpia errores
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Establece filtro de vehículos
     */
    fun setVehicleFilter(vehicleType: VehicleType?) {
        _uiState.update { it.copy(vehicleFilter = vehicleType) }
    }
    
    
    /**
     * Limpia el vehículo seleccionado
     */
    fun clearSelectedVehicle() {
        _uiState.update { 
            it.copy(
                selectedVehicle = null,
                meetingPoint = null,
                eta = null
            ) 
        }
    }

    /**
     * Actualiza el radio de búsqueda
     */
    fun updateSearchRadius(radiusKm: Double) {
        _uiState.update { it.copy(searchRadius = radiusKm) }
        startVehicleUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        locationUpdatesJob?.cancel()
        vehicleUpdatesJob?.cancel()
        etaUpdateJob?.cancel()
    }
}
