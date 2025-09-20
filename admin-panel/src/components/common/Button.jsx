import React from 'react';

const Button = ({ 
  children, 
  onClick, 
  loading = false, 
  disabled = false, 
  variant = "primary", 
  size = "medium",
  className = "",
  style = {}
}) => {
  const baseStyles = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    border: 'none',
    borderRadius: '0.75rem',
    fontWeight: '500',
    cursor: loading || disabled ? 'not-allowed' : 'pointer',
    transition: 'all 0.2s',
    opacity: loading || disabled ? 0.5 : 1,
    ...style
  };

  const variants = {
    primary: {
      background: 'linear-gradient(45deg, #2563eb, #4f46e5)',
      color: 'white',
      boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
    },
    secondary: {
      backgroundColor: 'white',
      color: '#374151',
      border: '1px solid #d1d5db',
      boxShadow: '0 1px 2px 0 rgba(0, 0, 0, 0.05)'
    }
  };

  const sizes = {
    small: {
      padding: '0.5rem 0.75rem',
      fontSize: '0.875rem'
    },
    medium: {
      padding: '0.75rem 1rem',
      fontSize: '1rem'
    },
    large: {
      padding: '1rem 1.5rem',
      fontSize: '1.125rem'
    }
  };

  const buttonStyles = {
    ...baseStyles,
    ...variants[variant],
    ...sizes[size]
  };

  return (
    <button
      onClick={loading || disabled ? undefined : onClick}
      disabled={loading || disabled}
      style={buttonStyles}
      className={className}
    >
      {loading ? (
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <div style={{
            animation: 'spin 1s linear infinite',
            borderRadius: '50%',
            width: '1.25rem',
            height: '1.25rem',
            border: '2px solid transparent',
            borderTop: '2px solid currentColor',
            marginRight: '0.5rem'
          }}></div>
          Procesando...
        </div>
      ) : (
        children
      )}
      <style jsx>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </button>
  );
};

export default Button;