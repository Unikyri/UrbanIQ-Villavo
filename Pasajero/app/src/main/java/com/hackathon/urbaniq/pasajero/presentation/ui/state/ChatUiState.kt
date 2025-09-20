package com.hackathon.urbaniq.pasajero.presentation.ui.state

import com.hackathon.urbaniq.pasajero.domain.model.Route

/**
 * Representa un mensaje en el chat
 */
data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedRoutes: List<Route> = emptyList(),
    val isError: Boolean = false
)

/**
 * Estado UI para la pantalla de chat con IA
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val isListening: Boolean = false, // Para Speech-to-Text
    val isSpeaking: Boolean = false,  // Para Text-to-Speech
    val error: String? = null,
    val isVoiceInputEnabled: Boolean = false,
    val availableRoutes: List<Route> = emptyList()
) {
    /**
     * Obtiene el último mensaje del asistente
     */
    fun getLastAssistantMessage(): ChatMessage? {
        return messages.lastOrNull { !it.isFromUser }
    }
    
    /**
     * Obtiene el último mensaje del usuario
     */
    fun getLastUserMessage(): ChatMessage? {
        return messages.lastOrNull { it.isFromUser }
    }
    
    /**
     * Indica si el chat está vacío
     */
    fun isEmpty(): Boolean = messages.isEmpty()
    
    /**
     * Indica si hay algún error
     */
    fun hasError(): Boolean = error != null
    
    /**
     * Indica si se puede enviar un mensaje
     */
    fun canSendMessage(): Boolean = currentMessage.isNotBlank() && !isLoading
}
