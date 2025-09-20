package com.hackathon.urbaniq.pasajero.domain.model

import com.google.firebase.Timestamp

/**
 * Información de la billetera digital del usuario
 */
data class WalletInfo(
    val userId: String,
    val balance: Double,
    val lastUpdated: Timestamp,
    val isActive: Boolean = true,
    val currency: String = "COP" // Pesos colombianos
) {
    /**
     * Verifica si hay suficiente saldo para un pago
     */
    fun hasSufficientBalance(amount: Double): Boolean {
        return balance >= amount
    }
    
    /**
     * Formatea el saldo como moneda
     */
    fun getFormattedBalance(): String {
        return "$ ${String.format("%,.0f", balance)} $currency"
    }
}
