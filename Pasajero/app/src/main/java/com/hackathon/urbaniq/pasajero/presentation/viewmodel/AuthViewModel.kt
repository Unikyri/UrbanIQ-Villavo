package com.hackathon.urbaniq.pasajero.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.urbaniq.pasajero.domain.model.User
import com.hackathon.urbaniq.pasajero.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para manejo de autenticación
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Observar cambios en el estado de autenticación
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                _uiState.update { 
                    it.copy(
                        currentUser = user,
                        isAuthenticated = user != null,
                        isLoading = false
                    ) 
                }
            }
        }
    }

    /**
     * Registra un nuevo usuario
     */
    fun registerUser(email: String, password: String, name: String) {
        if (!validateRegistrationInput(email, password, name)) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.registerUser(email, password, name).fold(
                onSuccess = { user ->
                    Log.d(TAG, "Usuario registrado exitosamente: ${user.name}")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isAuthenticated = true,
                            successMessage = "¡Cuenta creada exitosamente!"
                        ) 
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error al registrar usuario", exception)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(exception)
                        ) 
                    }
                }
            )
        }
    }

    /**
     * Inicia sesión de usuario
     */
    fun loginUser(email: String, password: String) {
        if (!validateLoginInput(email, password)) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.loginUser(email, password).fold(
                onSuccess = { user ->
                    Log.d(TAG, "Login exitoso: ${user.name}")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isAuthenticated = true,
                            successMessage = "¡Bienvenido ${user.name}!"
                        ) 
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error al iniciar sesión", exception)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(exception)
                        ) 
                    }
                }
            )
        }
    }

    /**
     * Cierra la sesión del usuario
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            authRepository.logout().fold(
                onSuccess = {
                    Log.d(TAG, "Sesión cerrada exitosamente")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentUser = null,
                            isAuthenticated = false,
                            successMessage = "Sesión cerrada"
                        ) 
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error al cerrar sesión", exception)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cerrar sesión"
                        ) 
                    }
                }
            )
        }
    }

    /**
     * Envía email de restablecimiento de contraseña
     */
    fun sendPasswordReset(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Ingresa un email válido") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.sendPasswordResetEmail(email).fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Email de restablecimiento enviado a $email"
                        ) 
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error al enviar email de restablecimiento", exception)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(exception)
                        ) 
                    }
                }
            )
        }
    }

    /**
     * Limpia mensajes de error y éxito
     */
    fun clearMessages() {
        _uiState.update { 
            it.copy(
                error = null,
                successMessage = null
            ) 
        }
    }

    /**
     * Valida entrada de registro
     */
    private fun validateRegistrationInput(email: String, password: String, name: String): Boolean {
        when {
            name.isBlank() -> {
                _uiState.update { it.copy(error = "El nombre es requerido") }
                return false
            }
            email.isBlank() -> {
                _uiState.update { it.copy(error = "El email es requerido") }
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.update { it.copy(error = "Ingresa un email válido") }
                return false
            }
            password.isBlank() -> {
                _uiState.update { it.copy(error = "La contraseña es requerida") }
                return false
            }
            password.length < 6 -> {
                _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
                return false
            }
        }
        return true
    }

    /**
     * Valida entrada de login
     */
    private fun validateLoginInput(email: String, password: String): Boolean {
        when {
            email.isBlank() -> {
                _uiState.update { it.copy(error = "El email es requerido") }
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.update { it.copy(error = "Ingresa un email válido") }
                return false
            }
            password.isBlank() -> {
                _uiState.update { it.copy(error = "La contraseña es requerida") }
                return false
            }
        }
        return true
    }

    /**
     * Convierte excepciones de Firebase en mensajes user-friendly
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when (exception.message) {
            "The email address is already in use by another account." -> 
                "Este email ya está registrado"
            "The password is invalid or the user does not have a password." -> 
                "Contraseña incorrecta"
            "There is no user record corresponding to this identifier. The user may have been deleted." -> 
                "No existe una cuenta con este email"
            "The email address is badly formatted." -> 
                "Formato de email inválido"
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> 
                "Error de conexión. Verifica tu internet"
            else -> exception.message ?: "Error desconocido"
        }
    }
}

/**
 * Estado de UI para autenticación
 */
data class AuthUiState(
    val currentUser: User? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
