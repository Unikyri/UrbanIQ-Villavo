package com.hackathon.urbaniq.pasajero.domain.model

import com.google.firebase.Timestamp

/**
 * Entidad que representa una transacción de pago
 */
data class Transaction(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicleLicensePlate: String,
    val routeName: String?,
    val amount: Double,
    val timestamp: Timestamp,
    val status: TransactionStatus,
    val paymentMethod: String = "Billetera Digital",
    val description: String? = null,
    val errorMessage: String? = null
) {
    /**
     * Devuelve una descripción legible de la transacción
     */
    fun getDisplayDescription(): String {
        return description ?: "Pago de pasaje - ${vehicleLicensePlate}"
    }
    
    /**
     * Indica si la transacción fue exitosa
     */
    fun isSuccessful(): Boolean {
        return status == TransactionStatus.COMPLETED
    }
}
