import React from 'react';
import { Mail, Lock, ArrowRight } from 'lucide-react';
import InputField from '../components/common/InputField';
import Button from '../components/common/Button';
import { useAuthForm } from '../hooks/useAuthForm';

const LoginPage = ({ onToggleMode, onSubmit }) => {
  const { 
    formData, 
    errors, 
    showPassword, 
    loading, 
    setLoading,
    handleInputChange, 
    validateForm,
    setShowPassword 
  } = useAuthForm(true);

  const handleSubmit = async () => {
    if (!validateForm()) return;
    
    setLoading(true);
    
    try {
      await onSubmit(formData);
    } catch (error) {
      console.error('Login error:', error);
    } finally {
      setLoading(false);
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
    marginBottom: '1.5rem'
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

  const toggleContainerStyles = {
    textAlign: 'center',
    marginTop: '1.5rem'
  };

  const toggleTextStyles = {
    color: '#6b7280',
    margin: '0 0 0.5rem 0'
  };

  const toggleButtonStyles = {
    background: 'none',
    border: 'none',
    color: '#2563eb',
    fontWeight: '500',
    cursor: 'pointer',
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.25rem',
    fontSize: '1rem'
  };

  return (
    <div style={cardStyles}>
      <div style={headerStyles}>
        <h2 style={titleStyles}>
          Iniciar Sesión
        </h2>
        <p style={subtitleStyles}>
          Accede a tu panel de administración
        </p>
      </div>

      <div style={formStyles}>
        <InputField
          id="email"
          name="email"
          type="email"
          value={formData.email}
          onChange={handleInputChange}
          placeholder="admin@empresa.com"
          label="Correo Electrónico"
          icon={Mail}
          error={errors.email}
        />

        <InputField
          id="password"
          name="password"
          type={showPassword ? 'text' : 'password'}
          value={formData.password}
          onChange={handleInputChange}
          placeholder="••••••••"
          label="Contraseña"
          icon={Lock}
          error={errors.password}
          showPasswordToggle={true}
          showPassword={showPassword}
          onPasswordToggle={() => setShowPassword(!showPassword)}
        />

        <Button
          onClick={handleSubmit}
          loading={loading}
          style={{ width: '100%' }}
        >
          <div style={{ display: 'flex', alignItems: 'center' }}>
            Iniciar Sesión
            <ArrowRight size={20} style={{ marginLeft: '0.5rem' }} />
          </div>
        </Button>
      </div>

      <div style={toggleContainerStyles}>
        <p style={toggleTextStyles}>¿No tienes cuenta?</p>
        <button
          type="button"
          onClick={onToggleMode}
          style={toggleButtonStyles}
        >
          Regístrate
          <ArrowRight size={16} />
        </button>
      </div>
    </div>
  );
};

export default LoginPage;