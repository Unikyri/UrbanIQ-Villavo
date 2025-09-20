package com.hackathon.urbaniq.pasajero.presentation.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hackathon.urbaniq.pasajero.domain.model.PaymentCategory
import com.hackathon.urbaniq.pasajero.domain.model.PaymentMethod
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla para seleccionar método de recarga
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeMethodScreen(
    navController: NavController,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    val currencyFormatter = remember { 
        NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    }
    
    // Agrupar métodos por categoría
    val methodsByCategory = PaymentMethod.values().groupBy { it.category }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Métodos de Recarga") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            methodsByCategory.forEach { (category, methods) ->
                item {
                    // Título de categoría
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                items(methods) { method ->
                    PaymentMethodCard(
                        method = method,
                        currencyFormatter = currencyFormatter,
                        onClick = { onMethodSelected(method) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            item {
                // Información adicional
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Información",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "• Las recargas son instantáneas para billeteras digitales\n" +
                                    "• Los corresponsales pueden tardar hasta 5 minutos\n" +
                                    "• Monto mínimo: ${currencyFormatter.format(5000)}\n" +
                                    "• Monto máximo: ${currencyFormatter.format(500000)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del método de pago
            Icon(
                imageVector = getIconForPaymentMethod(method),
                contentDescription = method.displayName,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = method.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = method.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Min: ${currencyFormatter.format(method.minAmount)} - Max: ${currencyFormatter.format(method.maxAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Seleccionar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Obtiene el icono apropiado para cada método de pago
 */
@Composable
private fun getIconForPaymentMethod(method: PaymentMethod): ImageVector {
    return when (method) {
        PaymentMethod.NEQUI -> Icons.Default.Smartphone
        PaymentMethod.DAVIPLATA -> Icons.Default.Smartphone
        PaymentMethod.BANCOLOMBIA -> Icons.Default.AccountBalance
        PaymentMethod.EFECTY -> Icons.Default.Store
        PaymentMethod.BANCOLOMBIA_CORRESPONDENT -> Icons.Default.Store
        PaymentMethod.BALOTO -> Icons.Default.Store
        PaymentMethod.MANUAL_RECHARGE -> Icons.Default.Build
    }
}
