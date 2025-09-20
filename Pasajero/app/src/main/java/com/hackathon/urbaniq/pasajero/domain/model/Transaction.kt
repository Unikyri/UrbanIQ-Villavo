package com.hackathon.urbaniq.pasajero.domain.model

import com.google.firebase.Timestamp

/**
 * Entidad que representa una transacción según estructura Firestore
 */
data class Transaction(
    val id: String = "",
    val passengerId: String = "", // uid del pasajero actual
    val driverId: String = "", // uid del conductor del vehículo
    val vehicleId: String = "", // ID del vehículo escaneado
    val companyId: String = "", // ID de la empresa del vehículo
    val amount: Double = 0.0, // Monto del pasaje
    val timestamp: Timestamp = Timestamp.now() // Fecha y hora del pago
) {
    // Propiedades computadas para compatibilidad
    val userId: String get() = passengerId
    val status: TransactionStatus get() = TransactionStatus.COMPLETED
    val vehicleLicensePlate: String? get() = null
    val routeName: String? get() = null
    val paymentMethod: String get() = "Billetera Digital"
    val description: String? get() = "Pago de pasaje"
    val errorMessage: String? get() = null
    
    /**
     * Devuelve una descripción legible de la transacción
     */
    fun getDisplayDescription(): String {
        return description ?: "Pago de pasaje - Vehículo $vehicleId"
    }
    
    /**
     * Indica si la transacción fue exitosa
     */
    fun isSuccessful(): Boolean {
        return status == TransactionStatus.COMPLETED
    }
}