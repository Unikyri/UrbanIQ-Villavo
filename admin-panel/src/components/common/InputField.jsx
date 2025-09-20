import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

const InputField = ({ 
  id, 
  name, 
  type = "text", 
  value, 
  onChange, 
  placeholder, 
  label, 
  icon: Icon, 
  error,
  showPasswordToggle = false,
  showPassword = false,
  onPasswordToggle,
  style = {}
}) => {
  const [isFocused, setIsFocused] = useState(false);

  const containerStyles = {
    display: 'flex',
    flexDirection: 'column',
    marginBottom: '1rem',
    ...style
  };

  const labelStyles = {
    fontSize: '0.875rem',
    fontWeight: '500',
    color: '#374151',
    marginBottom: '0.5rem'
  };

  const inputWrapperStyles = {
    position: 'relative',
    display: 'flex',
    alignItems: 'center'
  };

  const iconStyles = {
    position: 'absolute',
    left: '0.75rem',
    top: '50%',
    transform: 'translateY(-50%)',
    pointerEvents: 'none',
    color: isFocused ? '#2563eb' : '#9ca3af',
    transition: 'color 0.2s ease',
    zIndex: 1
  };

  const inputStyles = {
    width: '100%',
    height: '48px', // Altura fija para consistencia
    paddingLeft: Icon ? '2.5rem' : '0.75rem',
    paddingRight: showPasswordToggle ? '2.75rem' : '0.75rem',
    border: `1px solid ${error ? '#ef4444' : (isFocused ? '#2563eb' : '#d1d5db')}`,
    borderRadius: '0.5rem',
    outline: 'none',
    transition: 'all 0.2s ease',
    fontSize: '1rem',
    backgroundColor: '#ffffff',
    boxShadow: isFocused 
      ? '0 0 0 3px rgba(37, 99, 235, 0.1)' 
      : '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    color: '#111827',
    boxSizing: 'border-box'
  };

  const toggleButtonStyles = {
    position: 'absolute',
    right: '0.75rem',
    top: '50%',
    transform: 'translateY(-50%)',
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    color: '#6b7280',
    padding: '0.25rem',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: '0.25rem',
    transition: 'color 0.2s ease',
    zIndex: 1
  };

  const errorStyles = {
    color: '#ef4444',
    fontSize: '0.875rem',
    marginTop: '0.25rem'
  };

  return (
    <div style={containerStyles}>
      <label htmlFor={id} style={labelStyles}>
        {label}
      </label>
      <div style={inputWrapperStyles}>
        {Icon && (
          <div style={iconStyles}>
            <Icon size={18} />
          </div>
        )}
        <input
          id={id}
          name={name}
          type={type}
          value={value}
          onChange={onChange}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          style={inputStyles}
          placeholder={placeholder}
        />
        {showPasswordToggle && (
          <button
            type="button"
            style={toggleButtonStyles}
            onClick={onPasswordToggle}
            onMouseEnter={(e) => {
              e.target.style.color = '#374151';
            }}
            onMouseLeave={(e) => {
              e.target.style.color = '#6b7280';
            }}
          >
            {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        )}
      </div>
      {error && (
        <p style={errorStyles}>
          {error}
        </p>
      )}
    </div>
  );
};

export default InputField;