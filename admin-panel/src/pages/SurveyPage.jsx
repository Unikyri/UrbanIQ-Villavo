import React, { useState } from 'react';
import InputField from '../components/common/InputField';
import Button from '../components/common/Button';


const SurveyPage = ({ onSubmit, loading }) => {
    const [formData, setFormData] = useState({
        companyName: '',
        nit: '',
        companyType: 'bus',
        drivers: '',
        vehicles: ''
    });

    const [errors, setErrors] = useState({});

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        
        // Limpiar error cuando el usuario empiece a escribir
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
    };

    const validateForm = () => {
        const newErrors = {};
        
        if (!formData.companyName.trim()) {
            newErrors.companyName = 'El nombre de la empresa es obligatorio';
        }
        
        if (!formData.nit.trim()) {
            newErrors.nit = 'El NIT es obligatorio';
        }
        
        if (!formData.drivers || formData.drivers <= 0) {
            newErrors.drivers = 'Debe ingresar un número válido de conductores';
        }
        
        if (!formData.vehicles || formData.vehicles <= 0) {
            newErrors.vehicles = 'Debe ingresar un número válido de vehículos';
        }
        
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async () => {
        console.log('Botón presionado, validando...', formData);
        
        if (!validateForm()) {
            console.log('Validación falló', errors);
            return;
        }
        
        try {
            console.log('Enviando datos:', formData);
            await onSubmit(formData);
        } catch (error) {
            console.error('Error en handleSubmit:', error);
            alert('Error: ' + error.message);
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

    const radioGroupStyle = {
        display: 'flex',
        gap: '1rem',
        marginTop: '0.5rem',
        justifyContent: 'center',
    };
    
    const radioLabelStyle = {
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
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

                <div style={{...formStyles, gap: '0.5rem'}}>
                    <label style={{ fontSize: '0.875rem', fontWeight: '500', color: '#374151', marginBottom: '0.5rem' }}>
                        Tipo de Empresa
                    </label>
                    <div style={radioGroupStyle}>
                        <label style={radioLabelStyle}>
                            <input
                                type="radio"
                                name="companyType"
                                value="bus"
                                checked={formData.companyType === 'bus'}
                                onChange={handleInputChange}
                            />
                            <span>Empresa de Buses</span>
                        </label>
                        <label style={radioLabelStyle}>
                            <input
                                type="radio"
                                name="companyType"
                                value="taxi"
                                checked={formData.companyType === 'taxi'}
                                onChange={handleInputChange}
                            />
                            <span>Empresa de Taxis</span>
                        </label>
                    </div>
                </div>

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