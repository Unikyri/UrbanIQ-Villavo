import React from 'react';

const AuthLayout = ({ children }) => {
  const containerStyles = {
    minHeight: '100vh',
    background: 'linear-gradient(135deg, #eff6ff 0%, #ffffff 50%, #eef2ff 100%)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '1rem',
    fontFamily: 'system-ui, -apple-system, sans-serif'
  };

  const wrapperStyles = {
    width: '100%',
    maxWidth: '28rem'
  };

  const footerStyles = {
    textAlign: 'center',
    marginTop: '2rem',
    fontSize: '0.875rem',
    color: '#6b7280'
  };

  return (
    <div style={containerStyles}>
      <div style={wrapperStyles}>
        {children}
        <div style={footerStyles}>
          © 2025 Urbaniq - Sistema de Gestión de Transporte
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;