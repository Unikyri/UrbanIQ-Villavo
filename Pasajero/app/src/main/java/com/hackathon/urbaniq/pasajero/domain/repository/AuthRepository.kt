package com.hackathon.urbaniq.pasajero.domain.repository

import com.hackathon.urbaniq.pasajero.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para operaciones de autenticación
 */
interface AuthRepository {
    /**
     * Registra un nuevo usuario con email y password
     */
    suspend fun registerUser(
        email: String,
        password: String,
        name: String
    ): Result<User>

    /**
     * Inicia sesión con email y password
     */
    suspend fun loginUser(
        email: String,
        password: String
    ): Result<User>

    /**
     * Cierra la sesión del usuario actual
     */
    suspend fun logout(): Result<Unit>

    /**
     * Obtiene el usuario actualmente autenticado
     */
    suspend fun getCurrentUser(): Result<User?>

    /**
     * Observa el estado de autenticación
     */
    fun observeAuthState(): Flow<User?>

    /**
     * Verifica si hay un usuario autenticado
     */
    suspend fun isUserLoggedIn(): Boolean

    /**
     * Obtiene el ID del usuario actual
     */
    suspend fun getCurrentUserId(): String?

    /**
     * Envía email de restablecimiento de contraseña
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Actualiza el perfil del usuario
     */
    suspend fun updateUserProfile(
        userId: String,
        name: String? = null,
        email: String? = null
    ): Result<User>
}
