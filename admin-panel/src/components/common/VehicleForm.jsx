import React from 'react';
import InputField from './InputField';
import Button from './Button';
import { Car, X } from 'lucide-react';
import { useAuthForm } from '../../hooks/useAuthForm';

const VehicleForm = ({ onSubmit, onCancel, loading, isEditing = false, initialData = {} }) => {
    const { 
        formData, 
        errors, 
        handleInputChange, 
        validateForm 
    } = useAuthForm(false, initialData);

    const handleSubmit = () => {
        if (validateForm()) {
            onSubmit(formData);
        }
    };

    const cardStyles = {
        backgroundColor: 'white',
        borderRadius: '1rem',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        border: '1px solid #f3f4f6',
        padding: '2rem'
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
            <div style={headerStyles}>
                <h2 style={{ fontSize: '1.5rem', fontWeight: '600', color: '#111827', margin: 0 }}>
                    {isEditing ? 'Editar Vehículo' : 'Añadir Vehículo'}
                </h2>
                <X style={closeButtonStyle} onClick={onCancel} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                <InputField
                    id="plate"
                    name="plate"
                    type="text"
                    value={formData.plate}
                    onChange={handleInputChange}
                    placeholder="Ej. ABC-123"
                    label="Placa del Vehículo"
                    icon={<Car />}
                    error={errors.plate}
                    disabled={isEditing}
                />
                
                <InputField
                    id="type"
                    name="type"
                    type="select"
                    value={formData.type}
                    onChange={handleInputChange}
                    label="Tipo de Vehículo"
                    error={errors.type}
                    options={[
                        { value: '', label: 'Selecciona una opción' },
                        { value: 'bus', label: 'Bus' },
                        { value: 'taxi', label: 'Taxi' }
                    ]}
                />
                
                <Button
                    onClick={handleSubmit}
                    loading={loading}
                    style={{ width: '100%', marginTop: '0.5rem' }}
                >
                    {isEditing ? 'Guardar Cambios' : 'Crear Vehículo'}
                </Button>
            </div>
        </div>
    );
};

export default VehicleForm;