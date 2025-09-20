package com.hackathon.urbaniq.pasajero.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hackathon.urbaniq.pasajero.core.Constants.Navigation
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.chat.ChatScreen
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.map.MapScreen
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.map.DebugMapScreen
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.vehicles.VehicleListScreen
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.payment.WalletScreen
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.payment.RechargeMethodScreen
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.payment.RechargeConfirmationScreen
import com.hackathon.urbaniq.pasajero.domain.model.PaymentMethod
import com.hackathon.urbaniq.pasajero.presentation.ui.screens.settings.SettingsScreen

/**
 * Elemento de navegación inferior
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Navegación principal de la aplicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrbanIQNavigation() {
    val navController = rememberNavController()
    
    val bottomNavItems = listOf(
        BottomNavItem(Navigation.MAP_SCREEN, Icons.Default.AccountBalanceWallet, "Vehículos"),
        BottomNavItem(Navigation.WALLET_SCREEN, Icons.Default.AccountBalanceWallet, "Billetera"),
        BottomNavItem(Navigation.CHAT_SCREEN, Icons.AutoMirrored.Filled.Chat, "Asistente"),
        BottomNavItem(Navigation.SETTINGS_SCREEN, Icons.Default.Settings, "Configuración")
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Navigation.MAP_SCREEN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Navigation.MAP_SCREEN) {
                VehicleListScreen(navController = navController) // Lista de vehículos sin mapa
            }
            
            composable(Navigation.WALLET_SCREEN) {
                WalletScreen(navController = navController)
            }
            
            composable(Navigation.CHAT_SCREEN) {
                ChatScreen(navController = navController)
            }
            
            composable(Navigation.SETTINGS_SCREEN) {
                SettingsScreen(navController = navController)
            }
            
            // Pantallas adicionales sin bottom navigation
            composable(Navigation.QR_SCANNER_SCREEN) {
                // QRScannerScreen(navController = navController)
            }
            
            composable(Navigation.PAYMENT_CONFIRMATION_SCREEN) {
                // PaymentConfirmationScreen(navController = navController)
            }
            
            // Pantallas de recarga
            composable("recharge_method") {
                RechargeMethodScreen(
                    navController = navController,
                    onMethodSelected = { method ->
                        navController.navigate("recharge_confirmation/${method.name}")
                    }
                )
            }
            
            composable("recharge_confirmation/{methodName}") { backStackEntry ->
                val methodName = backStackEntry.arguments?.getString("methodName")
                val paymentMethod = methodName?.let { 
                    try {
                        PaymentMethod.valueOf(it)
                    } catch (e: Exception) {
                        PaymentMethod.MANUAL_RECHARGE
                    }
                } ?: PaymentMethod.MANUAL_RECHARGE
                
                RechargeConfirmationScreen(
                    navController = navController,
                    paymentMethod = paymentMethod
                )
            }
        }
    }
}
