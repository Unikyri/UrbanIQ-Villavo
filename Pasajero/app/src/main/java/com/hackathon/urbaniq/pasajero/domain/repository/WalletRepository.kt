package com.hackathon.urbaniq.pasajero.domain.repository

import com.hackathon.urbaniq.pasajero.domain.model.Transaction
import com.hackathon.urbaniq.pasajero.domain.model.WalletInfo
import kotlinx.coroutines.flow.Flow

/**
 * Interface para el repositorio de billetera
 */
interface WalletRepository {
    
    /**
     * Obtiene la información de la billetera del usuario
     */
    suspend fun getWalletInfo(userId: String): Result<WalletInfo?>
    
    /**
     * Actualiza el saldo de la billetera
     */
    suspend fun updateBalance(userId: String, newBalance: Double): Result<Unit>
    
    /**
     * Agrega fondos a la billetera
     */
    suspend fun addFunds(userId: String, amount: Double): Result<WalletInfo>
    
    /**
     * Procesa un pago
     */
    suspend fun processPayment(
        userId: String,
        vehicleId: String,
        amount: Double
    ): Result<Transaction>
    
    /**
     * Obtiene el historial de transacciones del usuario
     */
    suspend fun getTransactionHistory(userId: String): Result<List<Transaction>>
    
    /**
     * Se suscribe a actualizaciones de billetera en tiempo real
     */
    fun subscribeToWalletUpdates(userId: String): Flow<WalletInfo?>
    
    /**
     * Valida si hay saldo suficiente para un pago
     */
    suspend fun validatePayment(userId: String, amount: Double): Result<Boolean>
    
    /**
     * Obtiene el ID del usuario actual
     */
    suspend fun getCurrentUserId(): String?
    
    /**
     * Crea una billetera por defecto para el usuario
     */
    suspend fun createDefaultWallet(userId: String): Result<WalletInfo>
}
