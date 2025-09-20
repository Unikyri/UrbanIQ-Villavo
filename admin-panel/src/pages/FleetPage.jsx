import React, { useState, useEffect } from 'react';
import { getAuth } from 'firebase/auth';
import { collection, query, where, getDocs, addDoc, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../config/firebase';

const FleetPage = () => {
    const [activeTab, setActiveTab] = useState('drivers');
    const [isFormVisible, setIsFormVisible] = useState(false);
    const [drivers, setDrivers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [routes, setRoutes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({ name: '', email: '', plate: '', type: 'bus', fare: '' });

    const auth = getAuth();
    const companyId = auth.currentUser ? auth.currentUser.uid : null;

    useEffect(() => {
        if (companyId) {
            fetchData();
        }
    }, [companyId, activeTab]);

    const fetchData = async () => {
        setLoading(true);
        try {
            // Fetch drivers
            const driversQuery = query(collection(db, 'users'), where('companyId', '==', companyId), where('role', '==', 'driver'));
            const driversSnapshot = await getDocs(driversQuery);
            const driversData = driversSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

            // Fetch vehicles
            const vehiclesQuery = query(collection(db, 'vehicles'), where('companyId', '==', companyId));
            const vehiclesSnapshot = await getDocs(vehiclesQuery);
            const vehiclesData = vehiclesSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

            // Fetch routes
            const routesQuery = query(collection(db, 'routes'), where('companyId', '==', companyId));
            const routesSnapshot = await getDocs(routesQuery);
            const routesData = routesSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

            setDrivers(driversData);
            setVehicles(vehiclesData);
            setRoutes(routesData);
        } catch (error) {
            console.error('Error fetching data:', error);
            alert('Error al cargar los datos: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async () => {
        if (!companyId) {
            alert('No se pudo determinar la empresa. Por favor, vuelve a iniciar sesión.');
            return;
        }

        setLoading(true);
        try {
            let docRef;
            if (activeTab === 'drivers') {
                if (!formData.name) {
                    alert('El nombre del conductor es requerido.');
                    return;
                }
                docRef = await addDoc(collection(db, 'users'), {
                    name: formData.name,
                    role: 'driver',
                    companyId: companyId,
                    createdAt: new Date().toISOString()
                });
            } else if (activeTab === 'vehicles') {
                if (!formData.plate) {
                    alert('La placa del vehículo es requerida.');
                    return;
                }
                docRef = await addDoc(collection(db, 'vehicles'), {
                    plate: formData.plate,
                    type: formData.type,
                    companyId: companyId,
                    status: 'active',
                    driverId: null,
                    createdAt: new Date().toISOString()
                });
            } else if (activeTab === 'routes') {
                if (!formData.name || !formData.fare) {
                    alert('El nombre y la tarifa de la ruta son requeridos.');
                    return;
                }
                docRef = await addDoc(collection(db, 'routes'), {
                    name: formData.name,
                    fare: Number(formData.fare),
                    companyId: companyId,
                    createdAt: new Date().toISOString(),
                    path: []
                });
            }
            
            setFormData({ name: '', email: '', plate: '', type: 'bus', fare: '' });
            setIsFormVisible(false);
            fetchData();
        } catch (error) {
            alert('Error al guardar: ' + error.message);
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id, collectionName) => {
        if (!window.confirm('¿Estás seguro de que quieres eliminar este elemento?')) return;
        
        setLoading(true);
        try {
            await deleteDoc(doc(db, collectionName, id));
            fetchData();
        } catch (error) {
            alert('Error al eliminar: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const tabStyles = (tabName) => ({
        padding: '0.75rem 1.5rem',
        cursor: 'pointer',
        fontWeight: activeTab === tabName ? '600' : '400',
        color: activeTab === tabName ? '#1f2937' : '#6b7280',
        borderBottom: activeTab === tabName ? '2px solid #2563eb' : '2px solid transparent',
        backgroundColor: 'transparent',
        border: 'none'
    });

    const buttonStyles = {
        padding: '0.75rem 1.5rem',
        backgroundColor: '#2563eb',
        color: 'white',
        border: 'none',
        borderRadius: '0.5rem',
        cursor: 'pointer',
        fontWeight: '500'
    };

    const inputStyles = {
        width: '100%',
        padding: '0.75rem',
        border: '1px solid #d1d5db',
        borderRadius: '0.5rem',
        fontSize: '1rem',
        marginBottom: '1rem'
    };

    return (
        <div style={{ padding: '1.5rem', backgroundColor: 'white', borderRadius: '1rem', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)', border: '1px solid #f3f4f6' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h1 style={{ fontSize: '1.875rem', fontWeight: '700', color: '#111827', margin: 0 }}>
                    Gestión de Flota
                </h1>
                {!isFormVisible && (
                    <button style={buttonStyles} onClick={() => setIsFormVisible(true)}>
                        {activeTab === 'drivers' ? 'Añadir Conductor' : activeTab === 'vehicles' ? 'Añadir Vehículo' : 'Añadir Ruta'}
                    </button>
                )}
            </div>

            {/* Tabs */}
            <div style={{ display: 'flex', borderBottom: '1px solid #e5e7eb', marginBottom: '1rem' }}>
                <button style={tabStyles('drivers')} onClick={() => setActiveTab('drivers')}>
                    Conductores
                </button>
                <button style={tabStyles('vehicles')} onClick={() => setActiveTab('vehicles')}>
                    Vehículos
                </button>
                <button style={tabStyles('routes')} onClick={() => setActiveTab('routes')}>
                    Rutas
                </button>
            </div>

            {/* Form */}
            {isFormVisible && (
                <div style={{ backgroundColor: '#f9fafb', padding: '1.5rem', borderRadius: '0.5rem', marginBottom: '1.5rem' }}>
                    <h3 style={{ marginTop: 0 }}>
                        {activeTab === 'drivers' ? 'Nuevo Conductor' : activeTab === 'vehicles' ? 'Nuevo Vehículo' : 'Nueva Ruta'}
                    </h3>
                    
                    {activeTab === 'drivers' ? (
                        <>
                            <input
                                type="text"
                                name="name"
                                placeholder="Nombre completo"
                                value={formData.name}
                                onChange={handleInputChange}
                                style={inputStyles}
                            />
                        </>
                    ) : activeTab === 'vehicles' ? (
                        <>
                            <input
                                type="text"
                                name="plate"
                                placeholder="Placa del vehículo"
                                value={formData.plate}
                                onChange={handleInputChange}
                                style={inputStyles}
                            />
                            <select
                                name="type"
                                value={formData.type}
                                onChange={handleInputChange}
                                style={inputStyles}
                            >
                                <option value="bus">Bus</option>
                                <option value="taxi">Taxi</option>
                            </select>
                        </>
                    ) : (
                        <>
                            <input
                                type="text"
                                name="name"
                                placeholder="Nombre de la ruta"
                                value={formData.name}
                                onChange={handleInputChange}
                                style={inputStyles}
                            />
                            <input
                                type="number"
                                name="fare"
                                placeholder="Costo del pasaje"
                                value={formData.fare}
                                onChange={handleInputChange}
                                style={inputStyles}
                            />
                        </>
                    )}
                    
                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <button
                            style={buttonStyles}
                            onClick={handleSubmit}
                            disabled={loading}
                        >
                            {loading ? 'Guardando...' : 'Guardar'}
                        </button>
                        <button
                            style={{...buttonStyles, backgroundColor: '#6b7280'}}
                            onClick={() => setIsFormVisible(false)}
                        >
                            Cancelar
                        </button>
                    </div>
                </div>
            )}

            {/* Content */}
            {loading ? (
                <p style={{ textAlign: 'center', color: '#6b7280' }}>Cargando...</p>
            ) : (
                <div>
                    {activeTab === 'drivers' ? (
                        <div>
                            <h3>Conductores ({drivers.length})</h3>
                            {drivers.length === 0 ? (
                                <p style={{ color: '#6b7280' }}>No hay conductores registrados</p>
                            ) : (
                                <div style={{ display: 'grid', gap: '1rem' }}>
                                    {drivers.map(driver => (
                                        <div key={driver.id} style={{ padding: '1rem', border: '1px solid #e5e7eb', borderRadius: '0.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <div>
                                                <strong>{driver.name}</strong>
                                                <p style={{ margin: '0.25rem 0 0 0', color: '#6b7280', fontSize: '0.875rem' }}>
                                                    Rol: {driver.role}
                                                </p>
                                            </div>
                                            <button
                                                onClick={() => handleDelete(driver.id, 'users')}
                                                style={{ ...buttonStyles, backgroundColor: '#ef4444', padding: '0.5rem 1rem' }}
                                            >
                                                Eliminar
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    ) : activeTab === 'vehicles' ? (
                        <div>
                            <h3>Vehículos ({vehicles.length})</h3>
                            {vehicles.length === 0 ? (
                                <p style={{ color: '#6b7280' }}>No hay vehículos registrados</p>
                            ) : (
                                <div style={{ display: 'grid', gap: '1rem' }}>
                                    {vehicles.map(vehicle => (
                                        <div key={vehicle.id} style={{ padding: '1rem', border: '1px solid #e5e7eb', borderRadius: '0.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <div>
                                                <strong>{vehicle.plate}</strong>
                                                <p style={{ margin: '0.25rem 0 0 0', color: '#6b7280', fontSize: '0.875rem' }}>
                                                    Tipo: {vehicle.type}
                                                </p>
                                            </div>
                                            <button
                                                onClick={() => handleDelete(vehicle.id, 'vehicles')}
                                                style={{ ...buttonStyles, backgroundColor: '#ef4444', padding: '0.5rem 1rem' }}
                                            >
                                                Eliminar
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    ) : (
                         <div>
                            <h3>Rutas ({routes.length})</h3>
                            {routes.length === 0 ? (
                                <p style={{ color: '#6b7280' }}>No hay rutas registradas</p>
                            ) : (
                                <div style={{ display: 'grid', gap: '1rem' }}>
                                    {routes.map(route => (
                                        <div key={route.id} style={{ padding: '1rem', border: '1px solid #e5e7eb', borderRadius: '0.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <div>
                                                <strong>{route.name}</strong>
                                                <p style={{ margin: '0.25rem 0 0 0', color: '#6b7280', fontSize: '0.875rem' }}>
                                                    Tarifa: ${route.fare}
                                                </p>
                                            </div>
                                            <button
                                                onClick={() => handleDelete(route.id, 'routes')}
                                                style={{ ...buttonStyles, backgroundColor: '#ef4444', padding: '0.5rem 1rem' }}
                                            >
                                                Eliminar
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default FleetPage;