package com.hackathon.urbaniq.pasajero.presentation.ui.state

import com.hackathon.urbaniq.pasajero.domain.model.Transaction
import com.hackathon.urbaniq.pasajero.domain.model.WalletInfo

/**
 * Estado UI para la pantalla de billetera
 */
data class WalletUiState(
    val walletInfo: WalletInfo? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val isRecharging: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val qrScannerActive: Boolean = false,
    val scannedQrCode: String? = null
) {
    /**
     * Obtiene el saldo formateado
     */
    fun getFormattedBalance(): String {
        return walletInfo?.getFormattedBalance() ?: "$ 0 COP"
    }
    
    /**
     * Obtiene el saldo actual
     */
    fun getCurrentBalance(): Double {
        return walletInfo?.balance ?: 0.0
    }
    
    /**
     * Verifica si hay suficiente saldo para un pago
     */
    fun hasSufficientBalance(amount: Double): Boolean {
        return walletInfo?.hasSufficientBalance(amount) ?: false
    }
    
    /**
     * Obtiene las transacciones ordenadas por fecha (más recientes primero)
     */
    fun getSortedTransactions(): List<Transaction> {
        return transactions.sortedByDescending { it.timestamp.toDate() }
    }
    
    /**
     * Indica si hay algún error
     */
    fun hasError(): Boolean = error != null
    
    /**
     * Indica si hay algún mensaje de éxito
     */
    fun hasSuccessMessage(): Boolean = successMessage != null
}
