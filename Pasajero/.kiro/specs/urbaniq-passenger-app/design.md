# Documento de Diseño - App del Pasajero Urbaniq

## Visión General

La App del Pasajero Urbaniq es una aplicación Android nativa desarrollada en Kotlin con Jetpack Compose que proporciona una experiencia de transporte público moderna e inclusiva. La aplicación se integra con Firebase para backend, Google Maps para visualización geoespacial, y Gemini AI para asistencia inteligente de rutas.

### Objetivos de Diseño
- **Simplicidad**: Interfaz intuitiva que minimiza la curva de aprendizaje
- **Accesibilidad**: Soporte completo para usuarios con discapacidades visuales
- **Rendimiento**: Respuesta rápida y uso eficiente de recursos
- **Confiabilidad**: Operación estable con manejo robusto de errores

## Arquitectura

### Patrón Arquitectónico: MVVM + Clean Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   MainActivity  │  │  MapScreen      │  │ WalletScreen │ │
│  │   (Compose)     │  │  (Compose)      │  │  (Compose)   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
│           │                     │                   │        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   MainViewModel │  │  MapViewModel   │  │WalletViewModel│ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Use Cases     │  │   Repositories  │  │   Entities   │ │
│  │                 │  │   (Interfaces)  │  │              │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ Firebase        │  │ Google Maps     │  │ Gemini AI    │ │
│  │ Repository      │  │ Repository      │  │ Repository   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Stack Tecnológico

**Frontend:**
- **Kotlin** - Lenguaje principal
- **Jetpack Compose** - UI moderna y declarativa
- **Material Design 3** - Sistema de diseño consistente
- **Hilt** - Inyección de dependencias

**Backend & Servicios:**
- **Firebase Firestore** - Base de datos en tiempo real
- **Firebase Auth** - Autenticación de usuarios
- **Google Maps SDK** - Mapas y geolocalización
- **Google Play Services Location** - Servicios de ubicación
- **Gemini AI API** - Asistente inteligente

**Funcionalidades Especiales:**
- **ML Kit Barcode Scanning** - Escaneo de códigos QR
- **Android TTS/STT** - Accesibilidad por voz
- **Coroutines** - Programación asíncrona

## Componentes y Interfaces

### 1. Capa de Presentación (UI)

#### MainActivity
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Punto de entrada principal con navegación entre pantallas
    // Maneja permisos de ubicación y configuración inicial
}
```

#### Pantallas Principales

**MapScreen**
- Mapa interactivo con Google Maps Compose
- Filtros de vehículos (buses/taxis)
- Panel inferior para información de vehículos
- Botón flotante para acceder al chatbot

**WalletScreen**
- Visualización de saldo actual
- Historial de transacciones
- Funcionalidad de recarga (simulada)
- Escáner QR integrado

**ChatScreen**
- Interfaz de chat con Gemini AI
- Entrada de texto y voz
- Sugerencias de rutas con mapas

**SettingsScreen**
- Configuración de accesibilidad
- Preferencias de usuario
- Información de la cuenta

#### ViewModels

**MapViewModel**
```kotlin
@HiltViewModel
class MapViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val locationRepository: LocationRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {
    // Estado del mapa, vehículos y cálculos de ETA
}
```

### 2. Capa de Dominio

#### Entidades Principales

**Vehicle**
```kotlin
data class Vehicle(
    val id: String,
    val type: VehicleType, // BUS, TAXI
    val licensePlate: String,
    val routeId: String?,
    val currentLocation: LatLng,
    val isActive: Boolean,
    val lastUpdated: Timestamp
)
```

**Route**
```kotlin
data class Route(
    val id: String,
    val name: String,
    val polyline: List<LatLng>,
    val color: String,
    val isActive: Boolean
)
```

**Transaction**
```kotlin
data class Transaction(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val amount: Double,
    val timestamp: Timestamp,
    val status: TransactionStatus
)
```

#### Use Cases

**CalculateOptimalMeetingPointUseCase**
```kotlin
class CalculateOptimalMeetingPointUseCase @Inject constructor() {
    suspend operator fun invoke(
        userLocation: LatLng,
        vehicleRoute: List<LatLng>
    ): MeetingPoint
}
```

**ProcessPaymentUseCase**
```kotlin
class ProcessPaymentUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        userId: String,
        vehicleId: String,
        amount: Double
    ): Result<Transaction>
}
```

### 3. Capa de Datos

#### Repositorios

**VehicleRepository**
```kotlin
interface VehicleRepository {
    fun getVehiclesInRadius(center: LatLng, radiusKm: Double): Flow<List<Vehicle>>
    fun getVehiclesByType(type: VehicleType): Flow<List<Vehicle>>
    suspend fun getVehicleById(id: String): Vehicle?
}
```

**GeminiRepository**
```kotlin
interface GeminiRepository {
    suspend fun getRouteRecommendation(
        origin: LatLng,
        destination: LatLng,
        availableRoutes: List<Route>
    ): Result<RouteRecommendation>
}
```

## Modelos de Datos

### Estructura de Firebase Firestore

```
urbaniq/
├── users/
│   └── {userId}/
│       ├── profile: UserProfile
│       ├── wallet: WalletInfo
│       └── transactions/
│           └── {transactionId}: Transaction
├── vehicles/
│   └── {vehicleId}: Vehicle
├── routes/
│   └── {routeId}: Route
└── real_time_locations/
    └── {vehicleId}: VehicleLocation
```

### Modelos de Estado UI

```kotlin
data class MapUiState(
    val userLocation: LatLng? = null,
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val meetingPoint: MeetingPoint? = null,
    val eta: Duration? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class WalletUiState(
    val balance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val isPaymentEnabled: Boolean = false,
    val isLoading: Boolean = false
)
```

## Manejo de Errores

### Estrategia de Manejo de Errores

**Errores de Red**
```kotlin
sealed class NetworkError : Exception() {
    object NoConnection : NetworkError()
    object Timeout : NetworkError()
    data class ServerError(val code: Int) : NetworkError()
}
```

**Errores de Ubicación**
```kotlin
sealed class LocationError : Exception() {
    object PermissionDenied : LocationError()
    object ServiceDisabled : LocationError()
    object Unavailable : LocationError()
}
```

**Manejo Global**
- Retry automático para operaciones críticas
- Fallback a datos en caché cuando sea posible
- Mensajes de error user-friendly
- Logging detallado para debugging

## Estrategia de Testing

### Pirámide de Testing

**Unit Tests (70%)**
- ViewModels y lógica de negocio
- Use Cases y cálculos
- Repositorios con datos mock
- Utilidades y extensiones

**Integration Tests (20%)**
- Flujos completos de datos
- Integración con Firebase
- APIs externas con mocks

**UI Tests (10%)**
- Flujos críticos de usuario
- Navegación entre pantallas
- Accesibilidad

### Herramientas de Testing

```kotlin
// Unit Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.1.1")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

// UI Testing
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

// Integration Testing
testImplementation("com.google.firebase:firebase-firestore-ktx")
```

## Consideraciones de Accesibilidad

### Implementación del Modo Accesible

**Text-to-Speech Integration**
```kotlin
class AccessibilityManager @Inject constructor(
    private val context: Context
) {
    private val tts = TextToSpeech(context) { status ->
        // Configuración TTS
    }
    
    fun announceMessage(message: String) {
        if (isAccessibilityEnabled) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
}
```

**Compose Accessibility**
```kotlin
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    contentDescription: String
) {
    Button(
        onClick = onClick,
        modifier = Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Button
        }
    ) {
        Text(text)
    }
}
```

### Características de Accesibilidad

- **Navegación por TalkBack** - Todos los elementos tienen descripciones
- **Alto Contraste** - Temas adaptativos para visibilidad
- **Texto Grande** - Soporte para escalado de fuentes
- **Comandos de Voz** - STT para acciones principales
- **Feedback Háptico** - Vibraciones para confirmaciones

## Consideraciones de Rendimiento

### Optimizaciones de Mapa
- Clustering de vehículos en zoom bajo
- Actualización diferencial de posiciones
- Límite de vehículos mostrados simultáneamente

### Gestión de Memoria
- Lazy loading de listas grandes
- Caché inteligente de imágenes
- Limpieza automática de recursos

### Optimización de Red
- Compresión de datos de Firebase
- Batch operations para múltiples updates
- Offline-first approach con sincronización

## Seguridad

### Autenticación y Autorización
- Firebase Auth con verificación de teléfono
- Tokens JWT para APIs externas
- Validación server-side de transacciones

### Protección de Datos
- Encriptación de datos sensibles
- Validación de proximidad para pagos
- Audit trail de transacciones

### Configuración de Seguridad
```kotlin
// Reglas de Firestore Security
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /transactions/{transactionId} {
      allow read: if request.auth != null && 
                     request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && 
                       request.auth.uid == request.resource.data.userId;
    }
  }
}
```