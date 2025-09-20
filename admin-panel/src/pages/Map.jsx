import React, { useEffect, useRef, useState } from 'react';
import { Loader } from '@googlemaps/js-api-loader';
import { db } from '../config/firebase';
import { collection, query, where, onSnapshot, doc, setDoc, addDoc } from 'firebase/firestore';
import { authService } from '../services/authService';

const Map = () => {
    const mapRef = useRef(null);
    const googleMapsApiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
    const [vehicles, setVehicles] = useState([]);
    const [routes, setRoutes] = useState([]);
    const [companyData, setCompanyData] = useState(null);
    const [isDrawingMode, setIsDrawingMode] = useState(false);
    const [newRouteName, setNewRouteName] = useState('');
    const [drawnPolyline, setDrawnPolyline] = useState(null);
    const [loading, setLoading] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);
    
    const mapInstance = useRef(null);
    const drawingManagerRef = useRef(null);
    const markers = useRef({});
    const infoWindow = useRef(null);
    const routePolylines = useRef({});

    // Obtener usuario actual
    useEffect(() => {
        const unsubscribe = authService.onAuthStateChange((user) => {
            setCurrentUser(user);
        });
        return () => unsubscribe();
    }, []);

    // Verificar API key
    useEffect(() => {
        if (!googleMapsApiKey) {
            console.error('VITE_GOOGLE_MAPS_API_KEY no está configurada');
            return;
        }
    }, [googleMapsApiKey]);

    // Efecto para inicializar el mapa de Google
    useEffect(() => {
        if (!googleMapsApiKey) return;
        
        const loader = new Loader({
            apiKey: googleMapsApiKey,
            version: 'weekly',
            libraries: ['places', 'geometry', 'drawing']
        });

        loader.load().then(() => {
            const mapOptions = {
                center: { lat: 4.1444, lng: -73.6105 }, // Villavicencio
                zoom: 12,
                // Removemos mapId por ahora para evitar errores
            };
            mapInstance.current = new google.maps.Map(mapRef.current, mapOptions);
            infoWindow.current = new google.maps.InfoWindow();
            console.log('Google Maps cargado con éxito');
        }).catch(e => {
            console.error('Error cargando Google Maps:', e);
        });
    }, [googleMapsApiKey]);

    // Efecto para obtener los datos de la empresa
    useEffect(() => {
        if (!currentUser?.uid) return;
        
        const companyRef = doc(db, "companies", currentUser.uid);
        const unsubscribe = onSnapshot(companyRef, (docSnap) => {
            if (docSnap.exists()) {
                setCompanyData(docSnap.data());
            } else {
                console.log("No se encontró el documento de la empresa.");
            }
        });
        return () => unsubscribe();
    }, [currentUser]);

    // Efecto para escuchar los cambios en la ubicación de los vehículos en tiempo real
    useEffect(() => {
        if (!currentUser?.uid) return;
        
        const q = query(collection(db, 'vehicles'), where('companyId', '==', currentUser.uid));
        const unsubscribe = onSnapshot(q, (querySnapshot) => {
            const vehicleUpdates = [];
            querySnapshot.forEach((doc) => {
                const vehicleData = doc.data();
                vehicleUpdates.push({
                    id: doc.id,
                    ...vehicleData
                });
            });
            setVehicles(vehicleUpdates);
            console.log("Vehículos actualizados:", vehicleUpdates);
        }, (error) => {
            console.error("Error al escuchar cambios en la ubicación:", error);
        });
        return () => unsubscribe();
    }, [currentUser]);
    
    // Efecto para escuchar las rutas en tiempo real (solo si la empresa es de buses)
    useEffect(() => {
        if (!currentUser?.uid || companyData?.type !== 'bus') return;
        
        const q = query(collection(db, 'routes'), where('companyId', '==', currentUser.uid));
        const unsubscribe = onSnapshot(q, (querySnapshot) => {
            const routeUpdates = [];
            querySnapshot.forEach((doc) => {
                const routeData = doc.data();
                // Verificar si path existe y convertir a formato correcto
                let points = [];
                if (routeData.path && Array.isArray(routeData.path)) {
                    points = routeData.path.map(p => ({
                        lat: p.latitude || p.lat,
                        lng: p.longitude || p.lng
                    }));
                }
                routeUpdates.push({
                    id: doc.id,
                    ...routeData,
                    points: points
                });
            });
            setRoutes(routeUpdates);
            console.log("Rutas actualizadas:", routeUpdates);
        }, (error) => {
            console.error("Error al escuchar rutas:", error);
        });
        return () => unsubscribe();
    }, [currentUser, companyData]);

    // Efecto para gestionar el dibujo de las polilíneas de las rutas
    useEffect(() => {
        if (!mapInstance.current || companyData?.type !== 'bus') return;
        
        const activeRouteIds = new Set(routes.map(r => r.id));
        
        // Limpiar rutas que ya no existen
        Object.keys(routePolylines.current).forEach(routeId => {
            if (!activeRouteIds.has(routeId)) {
                routePolylines.current[routeId].setMap(null);
                delete routePolylines.current[routeId];
            }
        });

        // Crear o actualizar rutas
        routes.forEach(route => {
            if (route.points && route.points.length > 0) {
                if (routePolylines.current[route.id]) {
                    routePolylines.current[route.id].setPath(route.points);
                } else {
                    const polyline = new google.maps.Polyline({
                        path: route.points,
                        geodesic: true,
                        strokeColor: '#2563eb',
                        strokeOpacity: 0.8,
                        strokeWeight: 4,
                        map: mapInstance.current
                    });
                    routePolylines.current[route.id] = polyline;
                }
            }
        });
    }, [routes, companyData]);

    // Efecto para gestionar los marcadores en el mapa
    useEffect(() => {
        if (!mapInstance.current) return;
        
        const activeVehicleIds = new Set(vehicles.map(v => v.id));
        
        // Limpiar marcadores de vehículos que ya no existen
        Object.keys(markers.current).forEach(vehicleId => {
            if (!activeVehicleIds.has(vehicleId)) {
                markers.current[vehicleId].setMap(null);
                delete markers.current[vehicleId];
            }
        });

        // Crear o actualizar marcadores
        vehicles.forEach(vehicle => {
            if (vehicle.location) {
                const position = new google.maps.LatLng(
                    vehicle.location.latitude, 
                    vehicle.location.longitude
                );
                
                if (markers.current[vehicle.id]) {
                    markers.current[vehicle.id].setPosition(position);
                } else {
                    const marker = new google.maps.Marker({
                        position: position,
                        map: mapInstance.current,
                        title: `Placa: ${vehicle.plate}`,
                        icon: {
                            url: vehicle.type === 'bus' ? 
                                'https://maps.google.com/mapfiles/kml/shapes/bus.png' : 
                                'https://maps.google.com/mapfiles/kml/shapes/taxi.png',
                            scaledSize: new google.maps.Size(32, 32)
                        }
                    });
                    
                    marker.addListener('click', () => {
                        const route = routes.find(r => r.id === vehicle.routeId);
                        const content = `
                            <div>
                                <h4>Vehículo: ${vehicle.plate}</h4>
                                <p>Tipo: ${vehicle.type}</p>
                                <p>Estado: ${vehicle.status}</p>
                                ${vehicle.driverId ? `<p>Conductor: ${vehicle.driverId}</p>` : ''}
                                ${route ? `<p>Ruta: ${route.name}</p>` : ''}
                            </div>
                        `;
                        infoWindow.current.setContent(content);
                        infoWindow.current.open(mapInstance.current, marker);
                    });

                    markers.current[vehicle.id] = marker;
                }
            }
        });
    }, [vehicles, routes]);
    
    // Efecto para la herramienta de dibujo - CORREGIDO
    useEffect(() => {
        if (!mapInstance.current || companyData?.type !== 'bus') return;
        
        if (drawingManagerRef.current) {
            drawingManagerRef.current.setMap(null);
        }
        
        const drawingManager = new google.maps.drawing.DrawingManager({
            drawingMode: null, // Iniciar sin modo de dibujo
            drawingControl: false,
            polylineOptions: {
                strokeColor: '#ef4444',
                strokeOpacity: 0.8,
                strokeWeight: 5,
                geodesic: true,
                editable: true,
            }
        });
        
        drawingManager.setMap(mapInstance.current);
        drawingManagerRef.current = drawingManager;
        
        const listener = drawingManager.addListener('polylinecomplete', (polyline) => {
            setDrawnPolyline(polyline);
            console.log("Ruta dibujada. Puntos:", polyline.getPath().getArray());
            drawingManager.setDrawingMode(null);
        });

        return () => {
            if (listener) google.maps.event.removeListener(listener);
            if (drawingManager) drawingManager.setMap(null);
        };
    }, [companyData]);

    const handleStartDrawing = () => {
        if (!drawingManagerRef.current) {
            console.error('Drawing manager no está inicializado');
            return;
        }
        
        setIsDrawingMode(true);
        if (drawnPolyline) {
            drawnPolyline.setMap(null);
            setDrawnPolyline(null);
        }
        drawingManagerRef.current.setDrawingMode(google.maps.drawing.OverlayType.POLYLINE);
    };

    // Función corregida para guardar ruta
    const saveRoute = async (companyId, routeName, routePoints) => {
        try {
            const routeData = {
                name: routeName,
                companyId: companyId,
                path: routePoints, // Array de objetos {latitude, longitude}
                fare: 0, // Se puede configurar después
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString()
            };

            await addDoc(collection(db, 'routes'), routeData);
            console.log('Ruta guardada exitosamente');
        } catch (error) {
            console.error('Error guardando ruta:', error);
            throw error;
        }
    };

    const handleSaveRoute = async () => {
        if (!newRouteName.trim()) {
            alert('Por favor, ingresa un nombre para la ruta.');
            return;
        }
        
        if (!drawnPolyline) {
            alert('Por favor, dibuja una ruta primero.');
            return;
        }
        
        setLoading(true);
        try {
            const path = drawnPolyline.getPath().getArray();
            const routePoints = path.map(p => ({
                latitude: p.lat(),
                longitude: p.lng(),
            }));

            await saveRoute(currentUser.uid, newRouteName, routePoints);
            alert('¡Ruta guardada con éxito!');
            handleCancelDrawing();
        } catch (error) {
            alert('Error al guardar la ruta: ' + error.message);
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    const handleCancelDrawing = () => {
        setIsDrawingMode(false);
        setNewRouteName('');
        if (drawnPolyline) {
            drawnPolyline.setMap(null);
            setDrawnPolyline(null);
        }
        if (drawingManagerRef.current) {
            drawingManagerRef.current.setDrawingMode(null);
        }
    };

    // Mostrar mensaje si no hay API key
    if (!googleMapsApiKey) {
        return (
            <div style={{ padding: '1.5rem', textAlign: 'center' }}>
                <h2>Configuración requerida</h2>
                <p>Para usar el mapa, configura tu API key de Google Maps en el archivo .env:</p>
                <code style={{ 
                    backgroundColor: '#f3f4f6', 
                    padding: '0.5rem', 
                    borderRadius: '0.25rem',
                    display: 'block',
                    marginTop: '1rem'
                }}>
                    VITE_GOOGLE_MAPS_API_KEY=tu-api-key-aqui
                </code>
            </div>
        );
    }

    const mapContainerStyle = {
        height: 'calc(100vh - 80px)',
        width: '100%',
        borderRadius: '1rem',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        border: '1px solid #f3f4f6'
    };
    
    const uiContainerStyle = {
        padding: '1.5rem',
        backgroundColor: 'white',
        borderRadius: '1rem',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        border: '1px solid #f3f4f6',
        marginBottom: '1.5rem'
    };
    
    const buttonStyle = {
        padding: '0.75rem 1.5rem',
        backgroundColor: '#2563eb',
        color: 'white',
        borderRadius: '0.5rem',
        fontWeight: '600',
        cursor: 'pointer',
        border: 'none',
        fontSize: '1rem',
        transition: 'background-color 0.2s'
    };
    
    const inputStyle = {
        width: '100%',
        padding: '0.75rem',
        fontSize: '1rem',
        borderRadius: '0.25rem',
        border: '1px solid #d1d5db',
        boxSizing: 'border-box'
    };
    
    return (
        <div style={{ 
            padding: '1.5rem', 
            backgroundColor: 'white', 
            borderRadius: '1rem', 
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)', 
            border: '1px solid #f3f4f6' 
        }}>
            <h1 style={{ 
                fontSize: '1.875rem', 
                fontWeight: '700', 
                color: '#111827', 
                margin: '0 0 1.5rem 0' 
            }}>
                Mapa y Monitoreo
            </h1>
            
            {companyData?.type === 'bus' && (
                <div style={uiContainerStyle}>
                    {!isDrawingMode ? (
                        <button style={buttonStyle} onClick={handleStartDrawing}>
                            Dibujar Nueva Ruta
                        </button>
                    ) : (
                        <div>
                            <p style={{ margin: '0 0 1rem 0', color: '#4b5563' }}>
                                ¡Haz clic en el mapa para empezar a dibujar la ruta!
                            </p>
                            <button 
                                style={{...buttonStyle, backgroundColor: '#dc2626'}} 
                                onClick={handleCancelDrawing}
                            >
                                Cancelar
                            </button>
                        </div>
                    )}
                </div>
            )}
            
            {isDrawingMode && drawnPolyline && (
                <div style={{...uiContainerStyle, marginTop: '1.5rem'}}>
                    <h3 style={{ margin: '0 0 1rem 0' }}>Guardar Ruta</h3>
                    <input
                        type="text"
                        placeholder="Nombre de la ruta"
                        value={newRouteName}
                        onChange={(e) => setNewRouteName(e.target.value)}
                        style={inputStyle}
                    />
                    <button 
                        style={{...buttonStyle, marginTop: '1rem', opacity: loading ? 0.5 : 1}} 
                        onClick={handleSaveRoute}
                        disabled={loading}
                    >
                        {loading ? 'Guardando...' : 'Guardar Ruta'}
                    </button>
                </div>
            )}

            <div ref={mapRef} style={mapContainerStyle}></div>
        </div>
    );
};

export default Map;