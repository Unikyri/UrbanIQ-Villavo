import React from 'react';

const Logo = ({ size = "large" }) => {
  const sizeClasses = {
    small: { width: '3rem', height: '3rem' },
    medium: { width: '4rem', height: '4rem' },
    large: { width: '5rem', height: '5rem' }
  };

  const containerStyles = {
    textAlign: 'center'
  };

  const logoStyles = {
    ...sizeClasses[size],
    background: 'linear-gradient(45deg, #2563eb, #4f46e5)',
    borderRadius: '1rem',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    margin: '0 auto 1rem',
    boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)'
  };

  const logoTextStyles = {
    fontSize: '1.5rem',
    fontWeight: 'bold',
    color: 'white'
  };

  const titleStyles = {
    fontSize: '1.875rem',
    fontWeight: 'bold',
    color: '#111827',
    margin: '0 0 0.5rem 0'
  };

  const subtitleStyles = {
    color: '#6b7280',
    margin: 0
  };

  return (
    <div style={containerStyles}>
      <div style={logoStyles}>
        <span style={logoTextStyles}>U</span>
      </div>
      <h1 style={titleStyles}>
        Urbaniq
      </h1>
      <p style={subtitleStyles}>
        Panel de Administración
      </p>
    </div>
  );
};

export default Logo;