package com.hackathon.urbaniq.pasajero.domain.model

/**
 * Métodos de pago disponibles para recargar saldo
 */
enum class PaymentMethod(
    val displayName: String,
    val description: String,
    val iconResource: String, // Nombre del icono
    val category: PaymentCategory,
    val minAmount: Double = 5000.0,
    val maxAmount: Double = 500000.0
) {
    // Billeteras Digitales
    NEQUI(
        displayName = "Nequi",
        description = "Transfiere desde tu cuenta Nequi",
        iconResource = "ic_nequi",
        category = PaymentCategory.DIGITAL_WALLET
    ),
    DAVIPLATA(
        displayName = "DaviPlata",
        description = "Transfiere desde tu cuenta DaviPlata",
        iconResource = "ic_daviplata",
        category = PaymentCategory.DIGITAL_WALLET
    ),
    BANCOLOMBIA(
        displayName = "Bancolombia",
        description = "Transfiere desde tu cuenta Bancolombia",
        iconResource = "ic_bancolombia",
        category = PaymentCategory.DIGITAL_WALLET
    ),
    
    // Corresponsales
    EFECTY(
        displayName = "Efecty",
        description = "Recarga en cualquier punto Efecty",
        iconResource = "ic_efecty",
        category = PaymentCategory.CORRESPONDENT,
        minAmount = 10000.0
    ),
    BANCOLOMBIA_CORRESPONDENT(
        displayName = "Corresponsal Bancolombia",
        description = "Recarga en corresponsales Bancolombia",
        iconResource = "ic_bancolombia_correspondent",
        category = PaymentCategory.CORRESPONDENT,
        minAmount = 10000.0
    ),
    BALOTO(
        displayName = "Baloto",
        description = "Recarga en puntos Baloto",
        iconResource = "ic_baloto",
        category = PaymentCategory.CORRESPONDENT,
        minAmount = 10000.0
    ),
    
    // Manual (para MVP)
    MANUAL_RECHARGE(
        displayName = "Recarga Manual",
        description = "Recarga simulada para pruebas",
        iconResource = "ic_manual",
        category = PaymentCategory.MANUAL,
        minAmount = 1000.0
    )
}

/**
 * Categorías de métodos de pago
 */
enum class PaymentCategory(val displayName: String) {
    DIGITAL_WALLET("Billeteras Digitales"),
    CORRESPONDENT("Corresponsales"),
    MANUAL("Simulación MVP")
}

/**
 * Información de una recarga
 */
data class RechargeInfo(
    val method: PaymentMethod,
    val amount: Double,
    val reference: String? = null, // Referencia de pago externa
    val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val status: RechargeStatus = RechargeStatus.PENDING
)

/**
 * Estados de una recarga
 */
enum class RechargeStatus(val displayName: String) {
    PENDING("Pendiente"),
    PROCESSING("Procesando"),
    COMPLETED("Completada"),
    FAILED("Fallida"),
    CANCELLED("Cancelada")
}
