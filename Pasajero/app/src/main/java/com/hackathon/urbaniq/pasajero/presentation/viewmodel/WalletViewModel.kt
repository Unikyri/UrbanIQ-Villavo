package com.hackathon.urbaniq.pasajero.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.urbaniq.pasajero.core.Constants
import com.hackathon.urbaniq.pasajero.domain.model.Transaction
import com.hackathon.urbaniq.pasajero.domain.repository.WalletRepository
import com.hackathon.urbaniq.pasajero.presentation.ui.state.WalletUiState
import com.hackathon.urbaniq.pasajero.presentation.ui.state.UiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de billetera
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private var walletUpdatesJob: Job? = null
    private var currentUserId: String? = null

    init {
        Log.d("WalletViewModel", "Inicializando WalletViewModel")
        initializeWallet()
    }

    /**
     * Inicializa la billetera del usuario actual
     */
    private fun initializeWallet() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Obtener ID del usuario actual
                currentUserId = walletRepository.getCurrentUserId()
                
                if (currentUserId == null) {
                    Log.w("WalletViewModel", "Usuario no autenticado")
                    _uiState.update { 
                        it.copy(
                            error = "Usuario no autenticado",
                            isLoading = false
                        ) 
                    }
                    return@launch
                }

                Log.d("WalletViewModel", "Usuario autenticado: $currentUserId")
                
                // Cargar información de la billetera
                loadWalletInfo()
                
                // Cargar historial de transacciones
                loadTransactionHistory()
                
                // Suscribirse a actualizaciones de billetera
                subscribeToWalletUpdates()
                
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Error inicializando billetera", e)
                handleError(e, "Error al inicializar la billetera")
            }
        }
    }

    /**
     * Carga la información de la billetera
     */
    private fun loadWalletInfo() {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                walletRepository.getWalletInfo(userId).fold(
                    onSuccess = { walletInfo ->
                        if (walletInfo != null) {
                            Log.d("WalletViewModel", "Billetera cargada: saldo ${walletInfo.balance}")
                            _uiState.update { 
                                it.copy(
                                    balance = walletInfo.balance,
                                    isLoading = false
                                ) 
                            }
                        } else {
                            // Crear billetera por defecto
                            createDefaultWallet()
                        }
                    },
                    onFailure = { exception ->
                        Log.e("WalletViewModel", "Error cargando billetera", exception)
                        handleError(exception, "Error al cargar la billetera")
                    }
                )
            }
        }
    }

    /**
     * Crea una billetera por defecto para el usuario
     */
    private fun createDefaultWallet() {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                walletRepository.createDefaultWallet(userId).fold(
                    onSuccess = { walletInfo ->
                        Log.d("WalletViewModel", "Billetera por defecto creada")
                        _uiState.update { 
                            it.copy(
                                balance = walletInfo.balance,
                                isLoading = false
                            ) 
                        }
                    },
                    onFailure = { exception ->
                        Log.e("WalletViewModel", "Error creando billetera por defecto", exception)
                        handleError(exception, "Error al crear la billetera")
                    }
                )
            }
        }
    }

    /**
     * Carga el historial de transacciones
     */
    private fun loadTransactionHistory() {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                walletRepository.getTransactionHistory(userId).fold(
                    onSuccess = { transactions ->
                        Log.d("WalletViewModel", "Historial cargado: ${transactions.size} transacciones")
                        _uiState.update { it.copy(transactions = transactions) }
                    },
                    onFailure = { exception ->
                        Log.e("WalletViewModel", "Error cargando historial", exception)
                        // No mostramos error aquí para no interrumpir la carga de la billetera
                    }
                )
            }
        }
    }

    /**
     * Se suscribe a las actualizaciones en tiempo real de la billetera
     */
    private fun subscribeToWalletUpdates() {
        currentUserId?.let { userId ->
            walletUpdatesJob?.cancel()
            walletUpdatesJob = viewModelScope.launch {
                walletRepository.subscribeToWalletUpdates(userId)
                    .catch { exception ->
                        Log.e("WalletViewModel", "Error en actualizaciones de billetera", exception)
                    }
                    .collect { walletInfo ->
                        if (walletInfo != null) {
                            Log.d("WalletViewModel", "Actualización de billetera: saldo ${walletInfo.balance}")
                            _uiState.update { 
                                it.copy(balance = walletInfo.balance) 
                            }
                        }
                    }
            }
        }
    }

    /**
     * Recarga saldo con monto personalizado
     */
    fun rechargeBalance(customAmount: Double? = null) {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                    
                    val rechargeAmount = customAmount ?: Constants.Payment.RECHARGE_AMOUNT
                    Log.d("WalletViewModel", "Recargando saldo: $rechargeAmount")
                    
                    walletRepository.addFunds(userId, rechargeAmount).fold(
                        onSuccess = { walletInfo ->
                            Log.d("WalletViewModel", "Recarga exitosa: nuevo saldo ${walletInfo.balance}")
                            _uiState.update { 
                                it.copy(
                                    balance = walletInfo.balance,
                                    isLoading = false,
                                    lastRechargeAmount = rechargeAmount
                                ) 
                            }
                            
                            // Recargar historial para mostrar la nueva transacción
                            loadTransactionHistory()
                        },
                        onFailure = { exception ->
                            Log.e("WalletViewModel", "Error en recarga", exception)
                            handleError(exception, "Error al recargar saldo")
                        }
                    )
                } catch (e: Exception) {
                    Log.e("WalletViewModel", "Error inesperado en recarga", e)
                    handleError(e, "Error inesperado al recargar")
                }
            }
        }
    }

    /**
     * Procesa un pago
     */
    fun processPayment(vehicleId: String, amount: Double): Flow<Result<Transaction>> = flow {
        currentUserId?.let { userId ->
            try {
                Log.d("WalletViewModel", "Procesando pago: $amount para vehículo $vehicleId")
                
                _uiState.update { it.copy(isProcessingPayment = true, error = null) }
                
                // Validar que hay saldo suficiente
                walletRepository.validatePayment(userId, amount).fold(
                    onSuccess = { isValid ->
                        if (!isValid) {
                            emit(Result.failure(IllegalStateException("Saldo insuficiente")))
                            return@fold
                        }
                        
                        // Procesar pago
                        walletRepository.processPayment(userId, vehicleId, amount).fold(
                            onSuccess = { transaction ->
                                Log.d("WalletViewModel", "Pago exitoso: ${transaction.id}")
                                _uiState.update { 
                                    it.copy(
                                        isProcessingPayment = false,
                                        lastTransaction = transaction
                                    ) 
                                }
                                
                                // Recargar historial
                                loadTransactionHistory()
                                
                                emit(Result.success(transaction))
                            },
                            onFailure = { exception ->
                                Log.e("WalletViewModel", "Error procesando pago", exception)
                                _uiState.update { it.copy(isProcessingPayment = false) }
                                emit(Result.failure(exception))
                            }
                        )
                    },
                    onFailure = { exception ->
                        Log.e("WalletViewModel", "Error validando pago", exception)
                        _uiState.update { it.copy(isProcessingPayment = false) }
                        emit(Result.failure(exception))
                    }
                )
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Error inesperado procesando pago", e)
                _uiState.update { it.copy(isProcessingPayment = false) }
                emit(Result.failure(e))
            }
        } ?: emit(Result.failure(IllegalStateException("Usuario no autenticado")))
    }

    /**
     * Verifica si hay saldo suficiente para un pago
     */
    fun validateBalance(amount: Double): Boolean {
        val currentBalance = _uiState.value.balance
        return currentBalance >= amount
    }

    /**
     * Obtiene el saldo actual
     */
    fun getCurrentBalance(): Double {
        return _uiState.value.balance
    }

    /**
     * Limpia errores
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Actualiza el estado de habilitación de pago
     */
    fun updatePaymentEnabled(isEnabled: Boolean) {
        _uiState.update { it.copy(isPaymentEnabled = isEnabled) }
    }

    /**
     * Maneja errores y los convierte a mensajes user-friendly
     */
    private fun handleError(exception: Throwable, defaultMessage: String) {
        val errorMessage = when (exception) {
            is IllegalStateException -> exception.message ?: defaultMessage
            is SecurityException -> "Error de permisos"
            else -> defaultMessage
        }
        
        _uiState.update { 
            it.copy(
                error = errorMessage,
                isLoading = false,
                isProcessingPayment = false
            ) 
        }
    }

    /**
     * Refresca los datos de la billetera
     */
    fun refresh() {
        Log.d("WalletViewModel", "Refrescando datos de billetera")
        loadWalletInfo()
        loadTransactionHistory()
    }

    override fun onCleared() {
        super.onCleared()
        walletUpdatesJob?.cancel()
        Log.d("WalletViewModel", "WalletViewModel limpiado")
    }
}
