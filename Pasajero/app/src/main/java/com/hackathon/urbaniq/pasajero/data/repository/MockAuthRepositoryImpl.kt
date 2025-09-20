package com.hackathon.urbaniq.pasajero.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.hackathon.urbaniq.pasajero.core.Constants
import com.hackathon.urbaniq.pasajero.domain.model.User
import com.hackathon.urbaniq.pasajero.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación mock de autenticación para testing/MVP sin Firebase
 * Usa SharedPreferences para persistencia local
 */
@Singleton
class MockAuthRepositoryImpl @Inject constructor(
    private val context: Context
) : AuthRepository {

    companion object {
        private const val TAG = "MockAuthRepository"
        private const val PREFS_NAME = "urbaniq_auth"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_BALANCE = "user_balance"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _authState = MutableStateFlow<User?>(null)
    
    // Usuarios registrados simulados (en memoria)
    private val registeredUsers = mutableMapOf<String, UserCredentials>()
    
    data class UserCredentials(
        val email: String,
        val password: String,
        val user: User
    )

    init {
        // Cargar usuario si está logueado
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            val user = User(
                id = prefs.getString(KEY_USER_ID, "") ?: "",
                name = prefs.getString(KEY_USER_NAME, "") ?: "",
                email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
                role = "passenger",
                balance = prefs.getFloat(KEY_USER_BALANCE, Constants.Payment.INITIAL_BALANCE.toFloat()).toDouble()
            )
            _authState.value = user
        }
        
        // Agregar usuario de prueba
        val testUser = User(
            id = "test_user_123",
            name = "Usuario Prueba",
            email = "test@urbaniq.com",
            role = "passenger",
            balance = Constants.Payment.INITIAL_BALANCE
        )
        registeredUsers["test@urbaniq.com"] = UserCredentials(
            email = "test@urbaniq.com",
            password = "123456",
            user = testUser
        )
    }

    override suspend fun registerUser(
        email: String,
        password: String,
        name: String
    ): Result<User> {
        Log.d(TAG, "Registrando usuario mock: $email")
        
        // Simular delay de red
        delay(1000)
        
        // Verificar si ya existe
        if (registeredUsers.containsKey(email)) {
            return Result.failure(Exception("Este email ya está registrado"))
        }
        
        // Crear nuevo usuario
        val userId = "user_${System.currentTimeMillis()}"
        val user = User(
            id = userId,
            name = name,
            email = email,
            role = "passenger",
            balance = Constants.Payment.INITIAL_BALANCE
        )
        
        // Guardar en memoria
        registeredUsers[email] = UserCredentials(email, password, user)
        
        // Guardar en SharedPreferences
        saveUserSession(user)
        
        Log.d(TAG, "Usuario registrado exitosamente: $name")
        return Result.success(user)
    }

    override suspend fun loginUser(
        email: String,
        password: String
    ): Result<User> {
        Log.d(TAG, "Iniciando sesión mock: $email")
        
        // Simular delay de red
        delay(800)
        
        // Verificar credenciales
        val credentials = registeredUsers[email]
        if (credentials == null) {
            return Result.failure(Exception("No existe una cuenta con este email"))
        }
        
        if (credentials.password != password) {
            return Result.failure(Exception("Contraseña incorrecta"))
        }
        
        // Login exitoso
        saveUserSession(credentials.user)
        
        Log.d(TAG, "Login exitoso: ${credentials.user.name}")
        return Result.success(credentials.user)
    }

    override suspend fun logout(): Result<Unit> {
        Log.d(TAG, "Cerrando sesión mock")
        
        // Limpiar SharedPreferences
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_BALANCE)
            .apply()
        
        // Actualizar estado
        _authState.value = null
        
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return Result.success(_authState.value)
    }

    override fun observeAuthState(): Flow<User?> = callbackFlow {
        val listener: (User?) -> Unit = { user ->
            trySend(user)
        }
        
        // Enviar estado actual
        trySend(_authState.value)
        
        // Observar cambios
        val job = CoroutineScope(Dispatchers.Main).launch {
            _authState.collect { user ->
                listener(user)
            }
        }
        
        awaitClose { job.cancel() }
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return _authState.value != null
    }

    override suspend fun getCurrentUserId(): String? {
        return _authState.value?.id
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        Log.d(TAG, "Simulando envío de email de recuperación a: $email")
        
        delay(1000)
        
        if (!registeredUsers.containsKey(email)) {
            return Result.failure(Exception("No existe una cuenta con este email"))
        }
        
        return Result.success(Unit)
    }

    override suspend fun updateUserProfile(
        userId: String,
        name: String?,
        email: String?
    ): Result<User> {
        val currentUser = _authState.value
        if (currentUser == null || currentUser.id != userId) {
            return Result.failure(Exception("Usuario no encontrado"))
        }
        
        val updatedUser = currentUser.copy(
            name = name ?: currentUser.name,
            email = email ?: currentUser.email
        )
        
        // Actualizar en memoria
        val oldEmail = currentUser.email
        registeredUsers.remove(oldEmail)
        registeredUsers[updatedUser.email] = registeredUsers[oldEmail]?.copy(
            email = updatedUser.email,
            user = updatedUser
        ) ?: return Result.failure(Exception("Error al actualizar"))
        
        // Guardar sesión actualizada
        saveUserSession(updatedUser)
        
        return Result.success(updatedUser)
    }
    
    /**
     * Guarda la sesión del usuario en SharedPreferences
     */
    private fun saveUserSession(user: User) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putFloat(KEY_USER_BALANCE, user.balance.toFloat())
            .apply()
        
        _authState.value = user
    }
}
