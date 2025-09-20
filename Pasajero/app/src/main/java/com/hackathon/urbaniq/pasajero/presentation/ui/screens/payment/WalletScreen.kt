package com.hackathon.urbaniq.pasajero.presentation.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hackathon.urbaniq.pasajero.domain.model.TransactionStatus
import com.hackathon.urbaniq.pasajero.presentation.viewmodel.WalletViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de billetera digital
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavController,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyFormatter = remember { 
        NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    }
    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Saldo actual
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Billetera",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Saldo Actual",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = currencyFormatter.format(uiState.balance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Mostrar última recarga si existe
                uiState.lastRechargeAmount?.let { amount ->
                    Text(
                        text = "Última recarga: ${currencyFormatter.format(amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { 
                    // Navegar a selección de método de recarga
                    navController.navigate("recharge_method")
                },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recargar")
                }
            }
            
            OutlinedButton(
                onClick = { 
                    // TODO: Navegar a escáner QR
                    navController.navigate("qr_scanner") 
                },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading && uiState.isPaymentEnabled
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pagar")
            }
            
            IconButton(
                onClick = { viewModel.refresh() }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Historial de transacciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Historial de Transacciones",
                style = MaterialTheme.typography.titleLarge
            )
            
            Text(
                text = "${uiState.transactions.size} transacciones",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.transactions.isEmpty() && !uiState.isLoading) {
            // Estado vacío
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay transacciones",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tus pagos aparecerán aquí",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.transactions) { transaction ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = transaction.description ?: "Pago de pasaje",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Vehículo: ${transaction.vehicleId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "-${currencyFormatter.format(transaction.amount)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = when (transaction.status) {
                                            TransactionStatus.COMPLETED -> MaterialTheme.colorScheme.error
                                            TransactionStatus.PENDING -> MaterialTheme.colorScheme.primary
                                            TransactionStatus.FAILED -> MaterialTheme.colorScheme.outline
                                            TransactionStatus.CANCELLED -> MaterialTheme.colorScheme.outline
                                            TransactionStatus.REFUNDED -> MaterialTheme.colorScheme.secondary
                                        }
                                    )
                                    
                                    // Indicador de estado
                                    Text(
                                        text = when (transaction.status) {
                                            TransactionStatus.COMPLETED -> "Completado"
                                            TransactionStatus.PENDING -> "Pendiente"
                                            TransactionStatus.FAILED -> "Fallido"
                                            TransactionStatus.CANCELLED -> "Cancelado"
                                            TransactionStatus.REFUNDED -> "Reembolsado"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (transaction.status) {
                                            TransactionStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                                            TransactionStatus.PENDING -> MaterialTheme.colorScheme.secondary
                                            TransactionStatus.FAILED -> MaterialTheme.colorScheme.error
                                            TransactionStatus.CANCELLED -> MaterialTheme.colorScheme.outline
                                            TransactionStatus.REFUNDED -> MaterialTheme.colorScheme.tertiary
                                        }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = dateFormatter.format(transaction.timestamp.toDate()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Mostrar errores
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: Mostrar Snackbar con el error
            // Por ahora solo limpiar el error después de mostrarlo
            viewModel.clearError()
        }
    }
}