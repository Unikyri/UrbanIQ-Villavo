package com.hackathon.urbaniq.pasajero.domain.model

import com.google.firebase.Timestamp

/**
 * Entidad que representa un usuario del sistema
 */
data class User(
    val id: String,
    val name: String,
    val email: String?,
    val phoneNumber: String?,
    val isAccessibilityModeEnabled: Boolean = false,
    val createdAt: Timestamp,
    val lastActiveAt: Timestamp? = null,
    val profileImageUrl: String? = null
)
