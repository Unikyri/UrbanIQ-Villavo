package com.hackathon.urbaniq.pasajero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp
import com.hackathon.urbaniq.pasajero.core.Constants
import com.hackathon.urbaniq.pasajero.domain.model.Transaction
import com.hackathon.urbaniq.pasajero.domain.model.TransactionStatus
import com.hackathon.urbaniq.pasajero.domain.model.User
import com.hackathon.urbaniq.pasajero.domain.model.WalletInfo
import com.hackathon.urbaniq.pasajero.domain.repository.WalletRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Implementación del repositorio de billetera usando Firebase Firestore
 */
@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : WalletRepository {

    private val usersCollection = firestore.collection(Constants.Firebase.USERS_COLLECTION)
    private val transactionsCollection = firestore.collection(Constants.Firebase.TRANSACTIONS_COLLECTION)

    override suspend fun getWalletInfo(userId: String): Result<WalletInfo?> = 
        suspendCancellableCoroutine { continuation ->
            usersCollection.document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            val walletInfo = WalletInfo(
                                userId = user.id,
                                balance = user.balance,
                                lastUpdated = Timestamp.now(),
                                isActive = true
                            )
                            continuation.resume(Result.success(walletInfo))
                        } else {
                            continuation.resume(Result.success(null))
                        }
                    } else {
                        // Crear usuario por defecto si no existe
                        val defaultWallet = WalletInfo(
                            userId = userId,
                            balance = Constants.Payment.INITIAL_BALANCE,
                            lastUpdated = Timestamp.now(),
                            isActive = true
                        )
                        continuation.resume(Result.success(defaultWallet))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }

    override suspend fun updateBalance(userId: String, newBalance: Double): Result<Unit> = 
        suspendCancellableCoroutine { continuation ->
            usersCollection.document(userId)
                .update("balance", newBalance)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }

    override suspend fun addFunds(userId: String, amount: Double): Result<WalletInfo> = 
        suspendCancellableCoroutine { continuation ->
            firestore.runTransaction { transaction ->
                val walletRef = usersCollection.document(userId)
                    .collection("wallet")
                    .document("info")

                val walletSnapshot = transaction.get(walletRef)
                val currentBalance = if (walletSnapshot.exists()) {
                    walletSnapshot.getDouble("balance") ?: 0.0
                } else {
                    Constants.Payment.INITIAL_BALANCE
                }

                val newBalance = currentBalance + amount
                val updatedWallet = WalletInfo(
                    userId = userId,
                    balance = newBalance,
                    lastUpdated = Timestamp.now(),
                    isActive = true
                )

                transaction.set(walletRef, updatedWallet)
                updatedWallet
            }.addOnSuccessListener { walletInfo ->
                continuation.resume(Result.success(walletInfo))
            }.addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
        }

    override suspend fun processPayment(
        userId: String,
        vehicleId: String,
        amount: Double
    ): Result<Transaction> = suspendCancellableCoroutine { continuation ->
        
        firestore.runTransaction { transaction ->
            val walletRef = usersCollection.document(userId)
                .collection("wallet")
                .document("info")

            val walletSnapshot = transaction.get(walletRef)
            val currentBalance = walletSnapshot.getDouble("balance") ?: 0.0

            if (currentBalance < amount) {
                throw IllegalStateException("Saldo insuficiente")
            }

            // Crear transacción según estructura Firestore
            val newTransaction = Transaction(
                id = transactionsCollection.document().id,
                passengerId = userId,
                driverId = "", // Se obtendrá del vehículo escaneado
                vehicleId = vehicleId,
                companyId = "", // Se obtendrá del vehículo escaneado
                amount = amount,
                timestamp = Timestamp.now()
            )

            // Actualizar saldo
            val newBalance = currentBalance - amount
            val updatedWallet = WalletInfo(
                userId = userId,
                balance = newBalance,
                lastUpdated = Timestamp.now(),
                isActive = true
            )

            // Guardar transacción
            transaction.set(
                transactionsCollection.document(newTransaction.id),
                newTransaction
            )

            // Actualizar billetera
            transaction.set(walletRef, updatedWallet)

            newTransaction
        }.addOnSuccessListener { transaction ->
            continuation.resume(Result.success(transaction))
        }.addOnFailureListener { exception ->
            continuation.resume(Result.failure(exception))
        }
    }

    override suspend fun getTransactionHistory(userId: String): Result<List<Transaction>> = 
        suspendCancellableCoroutine { continuation ->
            transactionsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50) // Limitar a las últimas 50 transacciones
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val transactions = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(Transaction::class.java)
                    }
                    continuation.resume(Result.success(transactions))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }

    override fun subscribeToWalletUpdates(userId: String): Flow<WalletInfo?> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            listenerRegistration = usersCollection.document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject(User::class.java)
                        if (user != null) {
                            val walletInfo = WalletInfo(
                                userId = user.id,
                                balance = user.balance,
                                lastUpdated = Timestamp.now(),
                                isActive = true
                            )
                            trySend(walletInfo)
                        }
                    } else {
                        // Crear billetera por defecto
                        val defaultWallet = WalletInfo(
                            userId = userId,
                            balance = Constants.Payment.INITIAL_BALANCE,
                            lastUpdated = Timestamp.now(),
                            isActive = true
                        )
                        trySend(defaultWallet)
                    }
                }
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    override suspend fun validatePayment(userId: String, amount: Double): Result<Boolean> = 
        suspendCancellableCoroutine { continuation ->
            usersCollection.document(userId)
                .collection("wallet")
                .document("info")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentBalance = document.getDouble("balance") ?: 0.0
                        val isValid = currentBalance >= amount
                        continuation.resume(Result.success(isValid))
                    } else {
                        continuation.resume(Result.success(false))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun createDefaultWallet(userId: String): Result<WalletInfo> = 
        suspendCancellableCoroutine { continuation ->
            val defaultUser = User(
                id = userId,
                name = "Usuario",
                email = "",
                role = "passenger",
                balance = Constants.Payment.INITIAL_BALANCE
            )

            usersCollection.document(userId)
                .set(defaultUser)
                .addOnSuccessListener {
                    val walletInfo = WalletInfo(
                        userId = userId,
                        balance = Constants.Payment.INITIAL_BALANCE,
                        lastUpdated = Timestamp.now(),
                        isActive = true
                    )
                    continuation.resume(Result.success(walletInfo))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
}
