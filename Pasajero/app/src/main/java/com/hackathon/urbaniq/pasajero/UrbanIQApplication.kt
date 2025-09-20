package com.hackathon.urbaniq.pasajero

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase principal de la aplicación UrbanIQ Pasajero
 * Configurada con Hilt para inyección de dependencias
 */
@HiltAndroidApp
class UrbanIQApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Inicialización adicional si es necesaria
    }
}
