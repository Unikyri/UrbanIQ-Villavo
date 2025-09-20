import React, { useState, useEffect } from 'react';
import { getAuth } from 'firebase/auth';
import Button from '../components/common/Button';
import DriverForm from '../components/common/DriverForm';
import VehicleForm from '../components/common/VehicleForm';
import AssignDriverModal from '../components/common/AssignDriverModal';
import { 
    getDrivers, 
    createDriver, 
    updateDriver, 
    deleteDriver, 
    getVehicles, 
    createVehicle, 
    updateVehicle, 
    deleteVehicle,
    getAvailableDrivers,
    assignDriverToVehicle
} from '../services/firestoreApi';
import { Edit, Trash2, UserPlus } from 'lucide-react';

const FleetPage = () => {
    const [activeTab, setActiveTab] = useState('drivers');
    const [isFormVisible, setIsFormVisible] = useState(false);
    const [isAssignModalVisible, setIsAssignModalVisible] = useState(false);
    const [drivers, setDrivers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [availableDrivers, setAvailableDrivers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [editingDriver, setEditingDriver] = useState(null);
    const [editingVehicle, setEditingVehicle] = useState(null);
    const [vehicleToAssign, setVehicleToAssign] = useState(null);

    const auth = getAuth();
    const companyId = auth.currentUser ? auth.currentUser.uid : null;

    useEffect(() => {
        if (companyId) {
            fetchData();
        }
    }, [companyId]);

    const fetchData = async () => {
        setLoading(true);
        const [fetchedDrivers, fetchedVehicles] = await Promise.all([
            getDrivers(companyId),
            getVehicles(companyId)
        ]);
        setDrivers(fetchedDrivers);
        setVehicles(fetchedVehicles);
        setLoading(false);
    };

    const fetchAvailableDrivers = async () => {
        const fetchedDrivers = await getAvailableDrivers(companyId);
        setAvailableDrivers(fetchedDrivers);
    };

    const tabStyles = (tabName) => ({
        padding: '0.75rem 1.5rem',
        cursor: 'pointer',
        fontWeight: activeTab === tabName ? '600' : '400',
        color: activeTab === tabName ? '#1f2937' : '#6b7280',
        borderBottom: activeTab === tabName ? '2px solid #2563eb' : '2px solid transparent',
        transition: 'color 0.2s, border-bottom 0.2s',
        whiteSpace: 'nowrap'
    });

    const tableHeaderStyles = {
        backgroundColor: '#f9fafb',
        color: '#4b5563',
        fontWeight: '600',
        textTransform: 'uppercase',
        fontSize: '0.75rem',
        padding: '0.75rem 1.5rem',
        textAlign: 'left'
    };

    const tableRowStyles = {
        borderBottom: '1px solid #e5e7eb'
    };

    const tableCellStyles = {
        padding: '1rem 1.5rem',
        color: '#4b5563',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis'
    };
    
    const actionCellStyles = {
        display: 'flex',
        gap: '0.5rem',
        padding: '1rem 1.5rem'
    };

    const handleAddClick = () => {
        setIsFormVisible(true);
        setEditingDriver(null);
        setEditingVehicle(null);
    };

    const handleEditDriverClick = (driver) => {
        setEditingDriver(driver);
        setIsFormVisible(true);
    };
    
    const handleEditVehicleClick = (vehicle) => {
        setEditingVehicle(vehicle);
        setIsFormVisible(true);
    };

    const handleAssignDriverClick = (vehicle) => {
        setVehicleToAssign(vehicle);
        fetchAvailableDrivers();
        setIsAssignModalVisible(true);
    };

    const handleAssign = async (driverId) => {
        setLoading(true);
        try {
            const oldDriverId = vehicleToAssign.driverId;
            await assignDriverToVehicle(vehicleToAssign.id, driverId === '' ? null : driverId, oldDriverId);
            fetchData();
            handleCancelAssign();
        } catch (error) {
            alert('Error al asignar conductor: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteDriver = async (driverId) => {
        if (window.confirm("¿Estás seguro de que quieres eliminar este conductor?")) {
            setLoading(true);
            try {
                await deleteDriver(driverId);
                fetchData(); // Recargar la lista después de eliminar
            } catch (error) {
                alert('Error al eliminar el conductor.');
            } finally {
                setLoading(false);
            }
        }
    };
    
    const handleDeleteVehicle = async (vehicleId) => {
        if (window.confirm("¿Estás seguro de que quieres eliminar este vehículo?")) {
            setLoading(true);
            try {
                await deleteVehicle(vehicleId);
                fetchData(); // Recargar la lista después de eliminar
            } catch (error) {
                alert('Error al eliminar el vehículo.');
            } finally {
                setLoading(false);
            }
        }
    };
    
    const handleCancelForm = () => {
        setIsFormVisible(false);
        setEditingDriver(null);
        setEditingVehicle(null);
    };
    
    const handleCancelAssign = () => {
        setIsAssignModalVisible(false);
        setVehicleToAssign(null);
    };

    const handleDriverFormSubmit = async (formData) => {
        setLoading(true);
        try {
            if (editingDriver) {
                await updateDriver(editingDriver.id, { name: formData.name });
            } else {
                await createDriver(companyId, formData);
            }
            fetchData();
            handleCancelForm();
        } catch (error) {
            alert('Error al guardar el conductor: ' + error.message);
        } finally {
            setLoading(false);
        }
    };
    
    const handleVehicleFormSubmit = async (formData) => {
        setLoading(true);
        try {
            if (editingVehicle) {
                await updateVehicle(editingVehicle.id, formData);
            } else {
                await createVehicle(companyId, formData);
            }
            fetchData();
            handleCancelForm();
        } catch (error) {
            alert('Error al guardar el vehículo: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const getDriverName = (driverId) => {
        const driver = drivers.find(d => d.id === driverId);
        return driver ? driver.name : 'No Asignado';
    };
    
    return (
        <div style={{ padding: '1.5rem', backgroundColor: 'white', borderRadius: '1rem', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)', border: '1px solid #f3f4f6' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h1 style={{ fontSize: '1.875rem', fontWeight: '700', color: '#111827', margin: 0 }}>Gestión de Flota</h1>
                {!isFormVisible && !isAssignModalVisible && (
                    <Button onClick={handleAddClick}>
                        {activeTab === 'drivers' ? 'Añadir Conductor' : 'Añadir Vehículo'}
                    </Button>
                )}
            </div>

            <div style={{ display: 'flex', borderBottom: '1px solid #e5e7eb', marginBottom: '1rem' }}>
                <div style={tabStyles('drivers')} onClick={() => setActiveTab('drivers')}>
                    Conductores
                </div>
                <div style={tabStyles('vehicles')} onClick={() => setActiveTab('vehicles')}>
                    Vehículos
                </div>
            </div>

            {isFormVisible ? (
                activeTab === 'drivers' ? (
                    <DriverForm 
                        onSubmit={handleDriverFormSubmit}
                        onCancel={handleCancelForm}
                        loading={loading}
                        isEditing={!!editingDriver}
                        initialData={editingDriver}
                    />
                ) : (
                    <VehicleForm 
                        onSubmit={handleVehicleFormSubmit}
                        onCancel={handleCancelForm}
                        loading={loading}
                        isEditing={!!editingVehicle}
                        initialData={editingVehicle}
                    />
                )
            ) : (
                <>
                    {activeTab === 'drivers' ? (
                        <div>
                            {loading ? (
                                <p style={{ textAlign: 'center', color: '#6b7280' }}>Cargando conductores...</p>
                            ) : (
                                <div style={{ overflowX: 'auto' }}>
                                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                        <thead>
                                            <tr>
                                                <th style={tableHeaderStyles}>Nombre</th>
                                                <th style={tableHeaderStyles}>Correo</th>
                                                <th style={tableHeaderStyles}>Acciones</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {drivers.length > 0 ? (
                                                drivers.map(driver => (
                                                    <tr key={driver.id} style={tableRowStyles}>
                                                        <td style={tableCellStyles}>{driver.name}</td>
                                                        <td style={tableCellStyles}>{driver.email}</td>
                                                        <td style={actionCellStyles}>
                                                            <button 
                                                                onClick={() => handleEditDriverClick(driver)}
                                                                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#2563eb' }}
                                                            >
                                                                <Edit size={18} />
                                                            </button>
                                                            <button 
                                                                onClick={() => handleDeleteDriver(driver.id)}
                                                                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#ef4444' }}
                                                            >
                                                                <Trash2 size={18} />
                                                            </button>
                                                        </td>
                                                    </tr>
                                                ))
                                            ) : (
                                                <tr style={tableRowStyles}>
                                                    <td style={tableCellStyles} colSpan="3">
                                                        <p style={{ textAlign: 'center', color: '#6b7280' }}>
                                                            No hay conductores registrados.
                                                        </p>
                                                    </td>
                                                </tr>
                                            )}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    ) : (
                        <div>
                            {loading ? (
                                <p style={{ textAlign: 'center', color: '#6b7280' }}>Cargando vehículos...</p>
                            ) : (
                                <div style={{ overflowX: 'auto' }}>
                                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                        <thead>
                                            <tr>
                                                <th style={tableHeaderStyles}>Placa</th>
                                                <th style={tableHeaderStyles}>Tipo</th>
                                                <th style={tableHeaderStyles}>Asignado</th>
                                                <th style={tableHeaderStyles}>Acciones</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {vehicles.length > 0 ? (
                                                vehicles.map(vehicle => (
                                                    <tr key={vehicle.id} style={tableRowStyles}>
                                                        <td style={tableCellStyles}>{vehicle.plate}</td>
                                                        <td style={tableCellStyles}>{vehicle.type}</td>
                                                        <td style={tableCellStyles}>
                                                            {getDriverName(vehicle.driverId)}
                                                        </td>
                                                        <td style={actionCellStyles}>
                                                            <button 
                                                                onClick={() => handleAssignDriverClick(vehicle)}
                                                                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#2563eb' }}
                                                            >
                                                                <UserPlus size={18} />
                                                            </button>
                                                            <button 
                                                                onClick={() => handleEditVehicleClick(vehicle)}
                                                                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#2563eb' }}
                                                            >
                                                                <Edit size={18} />
                                                            </button>
                                                            <button 
                                                                onClick={() => handleDeleteVehicle(vehicle.id)}
                                                                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#ef4444' }}
                                                            >
                                                                <Trash2 size={18} />
                                                            </button>
                                                        </td>
                                                    </tr>
                                                ))
                                            ) : (
                                                <tr style={tableRowStyles}>
                                                    <td style={tableCellStyles} colSpan="4">
                                                        <p style={{ textAlign: 'center', color: '#6b7280' }}>
                                                            No hay vehículos registrados.
                                                        </p>
                                                    </td>
                                                </tr>
                                            )}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    )}
                </>
            )}
            
            {isAssignModalVisible && (
                <AssignDriverModal
                    onCancel={handleCancelAssign}
                    onAssign={handleAssign}
                    drivers={availableDrivers}
                    loading={loading}
                />
            )}
        </div>
    );
};

export default FleetPage;