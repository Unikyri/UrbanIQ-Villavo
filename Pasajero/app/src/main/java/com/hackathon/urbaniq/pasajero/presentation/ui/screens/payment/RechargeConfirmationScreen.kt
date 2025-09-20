package com.hackathon.urbaniq.pasajero.presentation.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.hackathon.urbaniq.pasajero.domain.model.PaymentMethod
import com.hackathon.urbaniq.pasajero.presentation.viewmodel.WalletViewModel
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de confirmación de recarga
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeConfirmationScreen(
    navController: NavController,
    paymentMethod: PaymentMethod,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var amount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    val currencyFormatter = remember { 
        NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    }
    
    // Montos predefinidos
    val predefinedAmounts = listOf(10000.0, 20000.0, 50000.0, 100000.0)
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Confirmar Recarga") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del método seleccionado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (paymentMethod) {
                            PaymentMethod.NEQUI, PaymentMethod.DAVIPLATA -> Icons.Default.Smartphone
                            PaymentMethod.BANCOLOMBIA -> Icons.Default.AccountBalance
                            PaymentMethod.EFECTY, PaymentMethod.BANCOLOMBIA_CORRESPONDENT, PaymentMethod.BALOTO -> Icons.Default.Store
                            PaymentMethod.MANUAL_RECHARGE -> Icons.Default.Build
                        },
                        contentDescription = paymentMethod.displayName,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = paymentMethod.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = paymentMethod.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Saldo actual
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Saldo Actual",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = currencyFormatter.format(uiState.balance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Selección de monto
            Text(
                text = "Selecciona el monto a recargar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            // Montos predefinidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                predefinedAmounts.forEach { predefinedAmount ->
                    FilterChip(
                        onClick = { amount = predefinedAmount.toInt().toString() },
                        label = { 
                            Text(
                                text = currencyFormatter.format(predefinedAmount),
                                style = MaterialTheme.typography.bodySmall
                            ) 
                        },
                        selected = amount == predefinedAmount.toInt().toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Campo de monto personalizado
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } && newValue.length <= 7) {
                        amount = newValue
                    }
                },
                label = { Text("Monto personalizado") },
                placeholder = { Text("Ingresa el monto") },
                leadingIcon = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Información de límites
            Text(
                text = "Monto mínimo: ${currencyFormatter.format(paymentMethod.minAmount)}\n" +
                        "Monto máximo: ${currencyFormatter.format(paymentMethod.maxAmount)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Botón de confirmación
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (amountDouble != null && 
                        amountDouble >= paymentMethod.minAmount && 
                        amountDouble <= paymentMethod.maxAmount) {
                        
                        isLoading = true
                        
                        // Simular proceso de recarga
                        if (paymentMethod == PaymentMethod.MANUAL_RECHARGE) {
                            // Recarga manual inmediata
                            viewModel.rechargeBalance(amountDouble)
                            showSuccessDialog = true
                            isLoading = false
                        } else {
                            // Simular proceso de pago externo
                            simulateExternalPayment(
                                paymentMethod = paymentMethod,
                                amount = amountDouble,
                                onSuccess = {
                                    viewModel.rechargeBalance(amountDouble)
                                    showSuccessDialog = true
                                    isLoading = false
                                },
                                onError = {
                                    isLoading = false
                                }
                            )
                        }
                    }
                },
                enabled = !isLoading && amount.toDoubleOrNull()?.let { 
                    it >= paymentMethod.minAmount && it <= paymentMethod.maxAmount 
                } == true,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Procesando...")
                } else {
                    Text("Confirmar Recarga")
                }
            }
        }
    }
    
    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                navController.popBackStack()
                navController.popBackStack()
            },
            title = { Text("¡Recarga Exitosa!") },
            text = { 
                Text("Se ha recargado ${currencyFormatter.format(amount.toDoubleOrNull() ?: 0.0)} a tu billetera exitosamente.")
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showSuccessDialog = false
                        navController.popBackStack()
                        navController.popBackStack()
                    }
                ) {
                    Text("Aceptar")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Éxito",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}

/**
 * Simula el proceso de pago externo
 */
private fun simulateExternalPayment(
    paymentMethod: PaymentMethod,
    amount: Double,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    // En una app real, aquí se integraría con las APIs de las billeteras digitales
    // o se generaría un código de recarga para corresponsales
    
    // Simulamos un delay de procesamiento
    CoroutineScope(Dispatchers.Main).launch {
        delay(2000) // 2 segundos de "procesamiento"
        
        // 95% de probabilidad de éxito para simular realismo
        if (Math.random() < 0.95) {
            onSuccess()
        } else {
            onError()
        }
    }
}
