package com.hackathon.urbaniq.pasajero.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.hackathon.urbaniq.pasajero.core.Constants
import com.hackathon.urbaniq.pasajero.domain.model.User
import com.hackathon.urbaniq.pasajero.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Implementación del repositorio de autenticación usando Firebase Auth y Firestore
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override suspend fun registerUser(
        email: String,
        password: String,
        name: String
    ): Result<User> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "Registrando usuario: $email")
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    // Crear usuario en Firestore
                    val user = User(
                        id = firebaseUser.uid,
                        name = name,
                        email = email,
                        role = "passenger",
                        balance = Constants.Payment.INITIAL_BALANCE
                    )
                    
                    // Guardar en Firestore
                    firestore.collection(Constants.Firebase.USERS_COLLECTION)
                        .document(firebaseUser.uid)
                        .set(user)
                        .addOnSuccessListener {
                            Log.d(TAG, "Usuario registrado exitosamente en Firestore")
                            continuation.resume(Result.success(user))
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error al guardar usuario en Firestore", exception)
                            continuation.resume(Result.failure(exception))
                        }
                } else {
                    val error = Exception("Error al crear usuario en Firebase Auth")
                    Log.e(TAG, "Error en registro", error)
                    continuation.resume(Result.failure(error))
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al registrar usuario", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    override suspend fun loginUser(
        email: String,
        password: String
    ): Result<User> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "Iniciando sesión: $email")
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    // Obtener datos del usuario desde Firestore
                    firestore.collection(Constants.Firebase.USERS_COLLECTION)
                        .document(firebaseUser.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                try {
                                    val user = document.toObject(User::class.java)?.copy(id = document.id)
                                    if (user != null) {
                                        Log.d(TAG, "Login exitoso para usuario: ${user.name}")
                                        continuation.resume(Result.success(user))
                                    } else {
                                        val error = Exception("Error al parsear datos del usuario")
                                        Log.e(TAG, "Error en login", error)
                                        continuation.resume(Result.failure(error))
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error al convertir documento a User", e)
                                    continuation.resume(Result.failure(e))
                                }
                            } else {
                                // Usuario no existe en Firestore, crear perfil básico
                                val user = User(
                                    id = firebaseUser.uid,
                                    name = firebaseUser.displayName ?: "Usuario",
                                    email = firebaseUser.email ?: email,
                                    role = "passenger",
                                    balance = Constants.Payment.INITIAL_BALANCE
                                )
                                
                                firestore.collection(Constants.Firebase.USERS_COLLECTION)
                                    .document(firebaseUser.uid)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Perfil de usuario creado en login")
                                        continuation.resume(Result.success(user))
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(TAG, "Error al crear perfil en login", exception)
                                        continuation.resume(Result.failure(exception))
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error al obtener datos del usuario", exception)
                            continuation.resume(Result.failure(exception))
                        }
                } else {
                    val error = Exception("Usuario nulo después del login")
                    Log.e(TAG, "Error en login", error)
                    continuation.resume(Result.failure(error))
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al iniciar sesión", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    override suspend fun logout(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "Cerrando sesión")
        try {
            auth.signOut()
            Log.d(TAG, "Sesión cerrada exitosamente")
            continuation.resume(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
            continuation.resume(Result.failure(e))
        }
    }

    override suspend fun getCurrentUser(): Result<User?> = suspendCancellableCoroutine { continuation ->
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            firestore.collection(Constants.Firebase.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        try {
                            val user = document.toObject(User::class.java)?.copy(id = document.id)
                            continuation.resume(Result.success(user))
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al obtener usuario actual", e)
                            continuation.resume(Result.failure(e))
                        }
                    } else {
                        continuation.resume(Result.success(null))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error al obtener usuario actual", exception)
                    continuation.resume(Result.failure(exception))
                }
        } else {
            continuation.resume(Result.success(null))
        }
    }

    override fun observeAuthState(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Usuario autenticado, obtener datos de Firestore
                firestore.collection(Constants.Firebase.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            try {
                                val user = document.toObject(User::class.java)?.copy(id = document.id)
                                trySend(user)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al observar estado de auth", e)
                                trySend(null)
                            }
                        } else {
                            trySend(null)
                        }
                    }
                    .addOnFailureListener {
                        trySend(null)
                    }
            } else {
                // Usuario no autenticado
                trySend(null)
            }
        }
        
        auth.addAuthStateListener(authStateListener)
        
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = 
        suspendCancellableCoroutine { continuation ->
            Log.d(TAG, "Enviando email de restablecimiento a: $email")
            
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Log.d(TAG, "Email de restablecimiento enviado")
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error al enviar email de restablecimiento", exception)
                    continuation.resume(Result.failure(exception))
                }
        }

    override suspend fun updateUserProfile(
        userId: String,
        name: String?,
        email: String?
    ): Result<User> = suspendCancellableCoroutine { continuation ->
        val updates = mutableMapOf<String, Any>()
        name?.let { updates["name"] = it }
        email?.let { updates["email"] = it }
        
        if (updates.isEmpty()) {
            continuation.resume(Result.failure(Exception("No hay datos para actualizar")))
            return@suspendCancellableCoroutine
        }
        
        firestore.collection(Constants.Firebase.USERS_COLLECTION)
            .document(userId)
            .update(updates)
            .addOnSuccessListener {
                // Obtener usuario actualizado
                firestore.collection(Constants.Firebase.USERS_COLLECTION)
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            try {
                                val user = document.toObject(User::class.java)?.copy(id = document.id)
                                if (user != null) {
                                    continuation.resume(Result.success(user))
                                } else {
                                    continuation.resume(Result.failure(Exception("Error al obtener usuario actualizado")))
                                }
                            } catch (e: Exception) {
                                continuation.resume(Result.failure(e))
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("Usuario no encontrado")))
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al actualizar perfil", exception)
                continuation.resume(Result.failure(exception))
            }
    }
}
