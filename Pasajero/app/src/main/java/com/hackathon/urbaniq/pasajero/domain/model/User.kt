package com.hackathon.urbaniq.pasajero.domain.model

import com.google.firebase.Timestamp

/**
 * Entidad que representa un usuario según estructura Firestore
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "passenger", // Siempre será "passenger" para esta app
    val balance: Double = 0.0 // Saldo de la billetera digital (actualizable)
) {
    // Propiedades computadas para compatibilidad
    val phoneNumber: String? get() = null
    val isAccessibilityModeEnabled: Boolean get() = false
    val createdAt: Timestamp get() = Timestamp.now()
    val lastActiveAt: Timestamp? get() = null
    val profileImageUrl: String? get() = null
}