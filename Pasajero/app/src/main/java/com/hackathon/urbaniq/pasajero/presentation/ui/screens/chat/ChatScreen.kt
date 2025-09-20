package com.hackathon.urbaniq.pasajero.presentation.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Pantalla de chat con asistente IA
 * TODO: Implementar lógica completa con ViewModel y Gemini AI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(@Suppress("UNUSED_PARAMETER") navController: NavController) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Mensajes de ejemplo
    val sampleMessages = listOf(
        ChatMessage("1", "¡Hola! Soy tu asistente de UrbanIQ. ¿Cómo puedo ayudarte hoy?", false),
        ChatMessage("2", "¿Cómo llego al centro comercial?", true),
        ChatMessage("3", "Te puedo ayudar con eso. Desde tu ubicación actual, puedes tomar la ruta 4 que te lleva directamente al centro comercial. El bus más cercano llegará en aproximadamente 8 minutos.", false)
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        TopAppBar(
            title = { 
                Text("Asistente UrbanIQ") 
            }
        )
        
        // Lista de mensajes
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleMessages) { message ->
                ChatMessageBubble(
                    message = message.content,
                    isFromUser = message.isFromUser
                )
            }
        }
        
        // Input de mensaje
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe tu mensaje...") },
                maxLines = 3
            )
            
            IconButton(
                onClick = { /* TODO: Implementar Speech-to-Text */ }
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Grabar mensaje")
            }
            
            FloatingActionButton(
                onClick = { 
                    if (messageText.isNotBlank()) {
                        // TODO: Enviar mensaje
                        messageText = ""
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar mensaje")
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: String,
    isFromUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromUser) 16.dp else 4.dp,
                bottomEnd = if (isFromUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromUser) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isFromUser) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Clase de datos temporal para los mensajes
private data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean
)
