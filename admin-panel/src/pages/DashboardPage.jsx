import React from 'react';

const DashboardPage = () => {
    return (
        <div style={{ backgroundColor: 'white', borderRadius: '1rem', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)', border: '1px solid #f3f4f6', padding: '2rem', textAlign: 'center' }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: '600', color: '#111827', marginBottom: '1rem' }}>¡Bienvenido al Panel de Urbaniq! 🎉</h2>
            <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>Este es el dashboard principal de tu empresa.</p>
            <p style={{ color: '#2563eb', fontWeight: '500', marginBottom: '2rem' }}>Aquí se mostrarán las estadísticas clave de tu operación (activos, ingresos, etc.).</p>
            <p style={{ color: '#dc2626', marginBottom: '1.5rem' }}>📋 <strong>Próximo paso:</strong> Implementar la sección de Flota (Tarea 1.8)</p>
        </div>
    );
};

export default DashboardPage;