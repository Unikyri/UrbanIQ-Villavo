# Documento de Requerimientos

## Introducción

La App del Pasajero de Urbaniq es la interfaz principal a través de la cual los ciudadanos de Villavicencio interactuarán con el sistema de transporte público modernizado. La aplicación busca transformar la experiencia del usuario proporcionando información en tiempo real, herramientas de planificación inteligentes y un sistema de pago digital accesible para todos. La app aborda el problema central de la falta de paraderos fijos en Villavicencio proporcionando certeza y reduciendo los tiempos de espera.

## Requerimientos

### Requerimiento 1: Mapa Interactivo y Geolocalización

**Historia de Usuario:** Como pasajero, quiero ver en un mapa interactivo los buses y taxis que están cerca de mí, para poder tener una idea inmediata de mis opciones de transporte.

#### Criterios de Aceptación

1. CUANDO la app inicia ENTONCES el sistema DEBERÁ mostrar un mapa centrado en la ubicación del usuario mostrando únicamente los vehículos activos (buses y taxis) dentro de un radio predefinido (ej. 2 km)
2. CUANDO el usuario aplica filtros ENTONCES el sistema DEBERÁ mostrar "Solo Buses", "Solo Taxis" o "Ambos" según la selección del usuario
3. CUANDO el usuario toca un vehículo en el mapa ENTONCES el sistema DEBERÁ mostrar la información del vehículo (placa, ruta) y calcular el Punto de Encuentro Óptimo en la ruta del vehículo
4. CUANDO se selecciona un vehículo ENTONCES el sistema DEBERÁ calcular y mostrar el ETA dinámico al Punto de Encuentro Óptimo que se actualiza en tiempo real mientras el usuario y el vehículo se mueven
5. CUANDO se calcula el Punto de Encuentro Óptimo ENTONCES el sistema DEBERÁ identificar el segmento más cercano de la polilínea de la ruta del bus a las coordenadas del usuario

### Requerimiento 2: Explorador de Rutas

**Historia de Usuario:** Como pasajero, quiero explorar y visualizar todas las rutas de autobuses disponibles en la ciudad, para poder planificar viajes futuros y entender la red de transporte.

#### Criterios de Aceptación

1. CUANDO el usuario accede al explorador de rutas ENTONCES el sistema DEBERÁ mostrar una lista de todas las rutas de autobuses disponibles
2. CUANDO el usuario selecciona una ruta de la lista ENTONCES el sistema DEBERÁ dibujar el trazado completo de la ruta en el mapa
3. CUANDO se selecciona una ruta ENTONCES el sistema DEBERÁ mostrar todos los buses que actualmente operan en esa ruta

### Requerimiento 3: Billetera Digital y Sistema de Pago

**Historia de Usuario:** Como pasajero, quiero gestionar una billetera digital y pagar pasajes escaneando códigos QR, para no depender del efectivo y tener un proceso de pago rápido y seguro.

#### Criterios de Aceptación

1. CUANDO el usuario accede a la sección de billetera ENTONCES el sistema DEBERÁ mostrar su saldo actual
2. CUANDO el usuario toca "Recargar Saldo" ENTONCES el sistema DEBERÁ añadir una cantidad fija de dinero para fines de demostración (simulación MVP)
3. CUANDO el usuario inicia el escaneo de QR ENTONCES el sistema DEBERÁ abrir la cámara en modo escáner
4. CUANDO el dispositivo del usuario está a corta distancia de un vehículo (menos de 20 metros) ENTONCES el sistema DEBERÁ habilitar el botón "Pagar"
5. CUANDO se confirma el pago ENTONCES el sistema DEBERÁ descontar el monto de la billetera del usuario y registrar la transacción en Firestore

### Requerimiento 4: Asistente de IA (Chatbot con Gemini)

**Historia de Usuario:** Como pasajero, quiero preguntarle a un chatbot "cómo llego a X lugar" en lenguaje natural, para obtener instrucciones claras y sencillas de ruta incluyendo transbordos si es necesario.

#### Criterios de Aceptación

1. CUANDO el usuario accede al chatbot ENTONCES el sistema DEBERÁ mostrar una interfaz de chat
2. CUANDO el usuario escribe su destino en lenguaje natural O lo selecciona en el mapa ENTONCES el sistema DEBERÁ procesar la solicitud
3. CUANDO se hace una solicitud de ruta ENTONCES el sistema DEBERÁ enviar la ubicación del usuario, destino e información de todas las rutas de buses disponibles a la API de Gemini
4. CUANDO la IA responde ENTONCES el sistema DEBERÁ procesar y presentar la respuesta de forma clara, indicando qué ruta tomar
5. CUANDO no hay ruta directa disponible ENTONCES el sistema DEBERÁ sugerir rutas con transbordo

### Requerimiento 5: Modo de Accesibilidad

**Historia de Usuario:** Como pasajero con discapacidad visual, quiero que la app me guíe por voz y responda a comandos de voz, para poder usar el transporte público de forma autónoma y segura.

#### Criterios de Aceptación

1. CUANDO el usuario accede a configuraciones ENTONCES el sistema DEBERÁ proporcionar una opción para activar/desactivar el "Modo Accesible"
2. CUANDO el Modo Accesible está activo ENTONCES el sistema DEBERÁ usar el motor Text-to-Speech (TTS) del sistema operativo para narrar información clave en pantalla
3. CUANDO el Modo Accesible está activo ENTONCES el sistema DEBERÁ permitir que las acciones principales (pago, chatbot) sean activadas mediante comandos de voz (Speech-to-Text)
4. CUANDO el Modo Accesible está activo ENTONCES el sistema DEBERÁ adaptar la interfaz con alto contraste, textos de mayor tamaño y descripciones de contenido para lectores de pantalla como TalkBack

### Requerimiento 6: Seguimiento de Vehículos en Tiempo Real

**Historia de Usuario:** Como pasajero, quiero saber exactamente cuándo llegará un bus a mi punto de encuentro, para poder gestionar mejor mi tiempo y no esperar innecesariamente.

#### Criterios de Aceptación

1. CUANDO se selecciona un vehículo ENTONCES el sistema DEBERÁ calcular el ETA en tiempo real al Punto de Encuentro Óptimo
2. CUANDO el usuario o el vehículo se mueven ENTONCES el sistema DEBERÁ recalcular y actualizar el ETA dinámicamente
3. CUANDO está en Modo Accesible ENTONCES el sistema DEBERÁ anunciar actualizaciones de ETA por voz (ej. "Bus de la ruta 4 se acerca. Llegará a tu punto de encuentro en 3 minutos")

### Requerimiento 7: Seguridad de Pago Basada en Proximidad

**Historia de Usuario:** Como pasajero, quiero que el botón de pagar solo se active cuando esté dentro o muy cerca del bus, para evitar pagos accidentales.

#### Criterios de Aceptación

1. CUANDO el usuario está a más de 20 metros de un vehículo ENTONCES el sistema DEBERÁ mantener el botón "Pagar" deshabilitado
2. CUANDO el usuario está dentro de 20 metros de un vehículo ENTONCES el sistema DEBERÁ habilitar el botón "Pagar" con indicación visual
3. CUANDO se intenta un pago fuera del rango de proximidad ENTONCES el sistema DEBERÁ prevenir la transacción

### Requerimiento 8: Gestión de Transacciones

**Historia de Usuario:** Como pasajero, quiero ver mi historial de pagos y transacciones de billetera, para poder rastrear mis gastos de transporte.

#### Criterios de Aceptación

1. CUANDO se completa un pago ENTONCES el sistema DEBERÁ registrar la transacción con marca de tiempo, monto, ruta e información del vehículo
2. CUANDO el usuario accede al historial de billetera ENTONCES el sistema DEBERÁ mostrar todas las transacciones previas
3. CUANDO una transacción falla ENTONCES el sistema NO DEBERÁ descontar dinero de la billetera y DEBERÁ notificar al usuario del fallo