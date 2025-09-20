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
    fun getWalletInfo(userId: String): Flow<WalletInfo?>
    
    /**
     * Actualiza el saldo de la billetera
     */
    suspend fun updateBalance(userId: String, newBalance: Double): Result<Unit>
    
    /**
     * Procesa un pago
     */
    suspend fun processPayment(
        userId: String,
        vehicleId: String,
        amount: Double
    ): Result<Transaction>
    
    /**
     * Recarga la billetera (simulación para MVP)
     */
    suspend fun rechargeWallet(userId: String, amount: Double): Result<Unit>
    
    /**
     * Obtiene el historial de transacciones del usuario
     */
    fun getTransactionHistory(userId: String): Flow<List<Transaction>>
    
    /**
     * Obtiene una transacción específica
     */
    suspend fun getTransactionById(transactionId: String): Result<Transaction?>
    
    /**
     * Cancela una transacción pendiente
     */
    suspend fun cancelTransaction(transactionId: String): Result<Unit>
    
    /**
     * Verifica si el usuario tiene suficiente saldo
     */
    suspend fun hasSufficientBalance(userId: String, amount: Double): Result<Boolean>
}
