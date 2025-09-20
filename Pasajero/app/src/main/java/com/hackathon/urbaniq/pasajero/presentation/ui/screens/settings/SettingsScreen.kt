package com.hackathon.urbaniq.pasajero.presentation.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Pantalla de configuración
 * TODO: Implementar lógica completa con preferencias persistentes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(@Suppress("UNUSED_PARAMETER") navController: NavController) {
    var accessibilityMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Configuración") }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Accesibilidad",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.Accessibility,
                    title = "Modo Accesible",
                    description = "Activa navegación por voz y TTS",
                    trailing = {
                        Switch(
                            checked = accessibilityMode,
                            onCheckedChange = { 
                                accessibilityMode = it
                                // TODO: Guardar preferencia
                            }
                        )
                    }
                )
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            item {
                Text(
                    text = "Notificaciones",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones Push",
                    description = "Recibe alertas de vehículos cercanos",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { 
                                notificationsEnabled = it
                                // TODO: Guardar preferencia
                            }
                        )
                    }
                )
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            item {
                Text(
                    text = "Cuenta",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.Person,
                    title = "Perfil de Usuario",
                    description = "Gestiona tu información personal",
                    onClick = { /* TODO: Navegar a perfil */ }
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.Security,
                    title = "Privacidad y Seguridad",
                    description = "Configuración de datos y ubicación",
                    onClick = { /* TODO: Navegar a privacidad */ }
                )
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            item {
                Text(
                    text = "Información",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "Acerca de UrbanIQ",
                    description = "Versión 1.0.0",
                    onClick = { /* TODO: Mostrar información */ }
                )
            }
            
            item {
                SettingItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Ayuda y Soporte",
                    description = "¿Necesitas ayuda?",
                    onClick = { /* TODO: Abrir ayuda */ }
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            trailing?.invoke()
        }
    }
}
