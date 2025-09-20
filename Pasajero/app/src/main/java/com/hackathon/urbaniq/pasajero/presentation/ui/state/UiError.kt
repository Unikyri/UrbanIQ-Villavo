package com.hackathon.urbaniq.pasajero.presentation.ui.state

/**
 * Sealed class para diferentes tipos de errores en la UI
 */
sealed class UiError(val message: String) {
    
    // Errores de red
    object NoConnection : UiError("Sin conexión a internet")
    object Timeout : UiError("Tiempo de espera agotado")
    data class ServerError(val code: Int) : UiError("Error del servidor ($code)")
    
    // Errores de ubicación
    object LocationPermissionDenied : UiError("Permisos de ubicación denegados")
    object LocationServiceDisabled : UiError("Servicio de ubicación deshabilitado")
    object LocationUnavailable : UiError("Ubicación no disponible")
    
    // Errores de cámara
    object CameraPermissionDenied : UiError("Permisos de cámara denegados")
    object CameraUnavailable : UiError("Cámara no disponible")
    
    // Errores de micrófono
    object MicrophonePermissionDenied : UiError("Permisos de micrófono denegados")
    object MicrophoneUnavailable : UiError("Micrófono no disponible")
    
    // Errores de pago
    object InsufficientBalance : UiError("Saldo insuficiente")
    object PaymentFailed : UiError("Error al procesar el pago")
    object VehicleTooFar : UiError("Vehículo demasiado lejos para pagar")
    
    // Errores de IA
    object GeminiApiError : UiError("Error en el servicio de IA")
    object InvalidGeminiResponse : UiError("Respuesta inválida del asistente")
    
    // Errores generales
    data class Unknown(val throwable: Throwable) : UiError("Error desconocido: ${throwable.message}")
    data class Custom(val customMessage: String) : UiError(customMessage)
    
    /**
     * Convierte el error a un mensaje amigable para el usuario
     */
    fun toUserFriendlyMessage(): String {
        return when (this) {
            is NoConnection -> "Verifica tu conexión a internet"
            is Timeout -> "La operación tardó demasiado. Intenta de nuevo"
            is LocationPermissionDenied -> "Necesitamos permisos de ubicación para funcionar"
            is LocationServiceDisabled -> "Activa la ubicación en tu dispositivo"
            is CameraPermissionDenied -> "Necesitamos permisos de cámara para escanear códigos QR"
            is InsufficientBalance -> "No tienes saldo suficiente. Recarga tu billetera"
            is VehicleTooFar -> "Acércate más al vehículo para pagar"
            else -> message
        }
    }
}
