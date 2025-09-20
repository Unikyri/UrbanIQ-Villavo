# 🔥 Instrucciones de Configuración Firebase

## ✅ Estado Actual
- **google-services.json** ✅ Configurado con datos reales
- **Project ID**: `villavo-conecta`
- **Package**: `com.hackathon.urbaniq.pasajero`

## 🚨 Configuración Requerida en Firebase Console

### 1. Habilitar Firebase Authentication

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona el proyecto: **villavo-conecta**
3. En el menú lateral → **Authentication**
4. Pestaña **Sign-in method**
5. Click en **Email/Password**
6. **Enable** → Toggle ON
7. **Save**

### 2. Configurar Firestore Database

1. En Firebase Console → **Firestore Database**
2. **Create database**
3. **Start in test mode** (para desarrollo)
4. Selecciona ubicación: **us-central** o **southamerica-east1**

### 3. Reglas de Firestore (Opcional - Ya configuradas)

Las reglas actuales permiten acceso completo para usuarios autenticados:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 🎯 Estructura de Datos Esperada

### Colecciones que usará la app:

```
villavo-conecta/
├── users/
│   └── {userId}/
│       ├── id: string
│       ├── name: string  
│       ├── email: string
│       ├── role: "passenger"
│       └── balance: number
├── vehicles/
│   └── {vehicleId}/
│       ├── location: geopoint
│       ├── status: "active" | "inactive"
│       ├── type: "bus" | "taxi"
│       ├── plate: string
│       ├── driverId: string
│       ├── companyId: string
│       └── routeId?: string
├── routes/
│   └── {routeId}/
│       ├── name: string
│       ├── path: array<geopoint>
│       └── fare: number
└── transactions/
    └── {transactionId}/
        ├── passengerId: string
        ├── driverId: string
        ├── vehicleId: string
        ├── companyId: string
        ├── amount: number
        └── timestamp: timestamp
```

## ✅ Verificación

Después de habilitar Authentication, la app debería:

1. **Permitir registro** de nuevos usuarios
2. **Crear automáticamente** perfil en `/users/{userId}`
3. **Asignar saldo inicial** de $15,000 COP
4. **Sincronizar billetera** en tiempo real
5. **Mantener sesión** entre reinicios

## 🚨 Posibles Errores

### Error: "API key not valid"
- **Causa**: Authentication no habilitado
- **Solución**: Seguir paso 1 arriba

### Error: "PERMISSION_DENIED"
- **Causa**: Reglas de Firestore restrictivas
- **Solución**: Verificar reglas en Firestore → Rules

### Error: "Network error"
- **Causa**: Problemas de conectividad
- **Solución**: Verificar internet y reintentar

## 📱 Cómo Probar

1. **Ejecutar app** → Pantalla de login
2. **Crear cuenta** → Registro con email/password real
3. **Verificar Firestore** → Debe aparecer usuario en consola
4. **Login** → Debe funcionar con credenciales creadas
5. **Billetera** → Debe mostrar $15,000 COP inicial

¡Una vez completada la configuración, tendrás autenticación real con Firebase! 🚀
