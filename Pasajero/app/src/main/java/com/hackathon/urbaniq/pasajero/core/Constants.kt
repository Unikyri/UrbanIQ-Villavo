package com.hackathon.urbaniq.pasajero.core

/**
 * Constantes globales de la aplicación UrbanIQ Pasajero
 */
object Constants {
    
    // Configuración de la aplicación
    const val APP_NAME = "UrbanIQ Pasajero"
    const val DEFAULT_ZOOM_LEVEL = 15f
    const val VEHICLE_SEARCH_RADIUS_KM = 2.0
    
    // Configuración de pagos
    const val PAYMENT_PROXIMITY_THRESHOLD_METERS = 20.0
    const val DEFAULT_BUS_FARE = 2500.0
    const val DEFAULT_TAXI_FARE = 3500.0
    const val WALLET_RECHARGE_AMOUNT = 50000.0
    
    // Configuración de tiempo
    const val LOCATION_UPDATE_INTERVAL_MS = 5000L
    const val VEHICLE_UPDATE_INTERVAL_MS = 3000L
    const val ETA_UPDATE_INTERVAL_MS = 10000L
    
    // Configuración de accesibilidad
    const val TTS_SPEECH_RATE = 1.0f
    const val TTS_PITCH = 1.0f
    
    // Configuración de red
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val RETRY_ATTEMPTS = 3
    
    // Payment
    object Payment {
        const val DEFAULT_FARE = 2500.0 // COP
        const val PROXIMITY_THRESHOLD_METERS = 20.0
        const val PAYMENT_TIMEOUT_SECONDS = 30L
        const val MAX_RETRY_ATTEMPTS = 3
        const val INITIAL_BALANCE = 15000.0 // COP
        const val RECHARGE_AMOUNT = 10000.0 // COP
    }
    
    // Firebase Collections
    object Firebase {
        const val USERS_COLLECTION = "users"
        const val VEHICLES_COLLECTION = "vehicles"
        const val ROUTES_COLLECTION = "routes"
        const val TRANSACTIONS_COLLECTION = "transactions"
        const val REAL_TIME_LOCATIONS_COLLECTION = "real_time_locations"
    }
    
    // Navegación
    object Navigation {
        const val MAP_SCREEN = "map"
        const val WALLET_SCREEN = "wallet"
        const val CHAT_SCREEN = "chat"
        const val SETTINGS_SCREEN = "settings"
        const val QR_SCANNER_SCREEN = "qr_scanner"
        const val PAYMENT_CONFIRMATION_SCREEN = "payment_confirmation"
    }
    
    // Preferencias SharedPreferences
    object Preferences {
        const val PREFS_NAME = "urbaniq_prefs"
        const val KEY_ACCESSIBILITY_MODE = "accessibility_mode"
        const val KEY_USER_ID = "user_id"
        const val KEY_FIRST_LAUNCH = "first_launch"
        const val KEY_SELECTED_VEHICLE_FILTER = "vehicle_filter"
    }
    
    // Configuración de Gemini AI
    object GeminiAI {
        const val MODEL_NAME = "gemini-pro"
        const val MAX_TOKENS = 1000
        const val TEMPERATURE = 0.7f
    }
}
