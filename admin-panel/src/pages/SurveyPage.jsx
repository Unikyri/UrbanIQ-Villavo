import React from 'react';
import InputField from '../components/common/InputField';
import Button from '../components/common/Button';
import { useAuthForm } from '../hooks/useAuthForm';

const SurveyPage = ({ onSubmit, loading }) => {
    const { 
        formData, 
        errors, 
        handleInputChange, 
        validateForm 
    } = useAuthForm(true);

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
        marginBottom: '1.5rem',
        textAlign: 'center'
    };

    const titleStyles = {
        fontSize: '1.5rem',
        fontWeight: '600',
        color: '#111827',
        margin: '0 0 0.5rem 0'
    };

    const subtitleStyles = {
        color: '#6b7280',
        margin: 0
    };

    const formStyles = {
        display: 'flex',
        flexDirection: 'column',
        gap: '1.25rem'
    };

    return (
        <div style={cardStyles}>
            <div style={headerStyles}>
                <h2 style={titleStyles}>
                    ¡Bienvenido a Urbaniq!
                </h2>
                <p style={subtitleStyles}>
                    Para empezar, completa la siguiente encuesta de caracterización de tu empresa.
                </p>
            </div>

            <div style={formStyles}>
                <InputField
                    id="companyName"
                    name="companyName"
                    type="text"
                    value={formData.companyName}
                    onChange={handleInputChange}
                    placeholder="Ej. Cootransmeta S.A."
                    label="Nombre de la Empresa"
                    error={errors.companyName}
                />
                
                <InputField
                    id="nit"
                    name="nit"
                    type="text"
                    value={formData.nit}
                    onChange={handleInputChange}
                    placeholder="Ej. 900123456-7"
                    label="NIT de la Empresa"
                    error={errors.nit}
                />

                <InputField
                    id="companyType"
                    name="companyType"
                    type="select"
                    value={formData.companyType}
                    onChange={handleInputChange}
                    label="Tipo de Empresa"
                    error={errors.companyType}
                    options={[
                        { value: '', label: 'Selecciona una opción' },
                        { value: 'bus', label: 'Empresa de Buses' },
                        { value: 'taxi', label: 'Empresa de Taxis' }
                    ]}
                />

                <InputField
                    id="drivers"
                    name="drivers"
                    type="number"
                    value={formData.drivers}
                    onChange={handleInputChange}
                    placeholder="Ej. 15"
                    label="Cantidad estimada de conductores"
                    error={errors.drivers}
                />
                
                <InputField
                    id="vehicles"
                    name="vehicles"
                    type="number"
                    value={formData.vehicles}
                    onChange={handleInputChange}
                    placeholder="Ej. 10"
                    label="Cantidad estimada de vehículos"
                    error={errors.vehicles}
                />
                
                <Button
                    onClick={handleSubmit}
                    loading={loading}
                    style={{ width: '100%' }}
                >
                    Finalizar y Entrar al Panel
                </Button>
            </div>
        </div>
    );
};

export default SurveyPage;