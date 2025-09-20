package com.hackathon.urbaniq.pasajero.domain.model

/**
 * Estados posibles de una transacción
 */
enum class TransactionStatus(val displayName: String) {
    PENDING("Pendiente"),
    COMPLETED("Completada"),
    FAILED("Fallida"),
    CANCELLED("Cancelada"),
    REFUNDED("Reembolsada")
}
