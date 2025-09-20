import React, { useState } from 'react';
import Button from './Button';
import { X, User } from 'lucide-react';
import InputField from './InputField';

const AssignDriverModal = ({ onCancel, onAssign, drivers, loading }) => {
    const [selectedDriverId, setSelectedDriverId] = useState('');

    const handleAssignClick = () => {
        if (selectedDriverId) {
            onAssign(selectedDriverId);
        } else {
            alert("Por favor, selecciona un conductor.");
        }
    };
    
    const cardStyles = {
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 1000
    };
    
    const contentStyles = {
        backgroundColor: 'white',
        borderRadius: '1rem',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        border: '1px solid #f3f4f6',
        padding: '2rem',
        width: '100%',
        maxWidth: '400px'
    };
    
    const headerStyles = {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '1.5rem',
        borderBottom: '1px solid #e5e7eb',
        paddingBottom: '1rem'
    };
    
    const closeButtonStyle = {
        cursor: 'pointer',
        color: '#6b7280',
        transition: 'color 0.2s'
    };

    return (
        <div style={cardStyles}>
            <div style={contentStyles}>
                <div style={headerStyles}>
                    <h2 style={{ fontSize: '1.5rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                        Asignar Conductor
                    </h2>
                    <X style={closeButtonStyle} onClick={onCancel} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                    <InputField
                        id="driver"
                        name="driver"
                        type="select"
                        value={selectedDriverId}
                        onChange={(e) => setSelectedDriverId(e.target.value)}
                        label="Selecciona un Conductor"
                        icon={<User />}
                        options={[
                            { value: '', label: 'Ninguno' },
                            ...drivers.map(d => ({ value: d.id, label: d.name }))
                        ]}
                    />
                    
                    <Button
                        onClick={handleAssignClick}
                        loading={loading}
                        style={{ width: '100%', marginTop: '0.5rem' }}
                    >
                        Asignar Conductor
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default AssignDriverModal;