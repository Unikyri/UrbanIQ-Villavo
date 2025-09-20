import React from 'react';
import { getAuth } from "firebase/auth";
import { LogOut, LayoutDashboard, Car, Map, FileText } from 'lucide-react';
import Logo from './Logo';

const DashboardLayout = ({ children, currentPage, onNavigate }) => {
    const handleLogout = async () => {
        try {
            const auth = getAuth();
            await auth.signOut();
            alert('Sesión cerrada exitosamente');
        } catch (error) {
            console.error('Error al cerrar sesión:', error);
        }
    };

    const navItemStyle = (isActive = false) => ({
        display: 'flex',
        alignItems: 'center',
        padding: '0.75rem 1.25rem',
        marginBottom: '0.5rem',
        borderRadius: '0.5rem',
        cursor: 'pointer',
        backgroundColor: isActive ? '#f3f4f6' : 'transparent',
        color: isActive ? '#1f2937' : '#6b7280',
        fontWeight: isActive ? '600' : '400',
        transition: 'background-color 0.2s, color 0.2s',
        gap: '0.75rem'
    });

    const iconStyle = {
        width: '1.25rem',
        height: '1.25rem',
        color: '#6b7280'
    };

    const sidebarStyle = {
        backgroundColor: 'white',
        width: '16rem',
        height: '100vh',
        padding: '1.5rem',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)'
    };

    const mainContentStyle = {
        flexGrow: 1,
        padding: '2rem',
        backgroundColor: '#f9fafb',
        height: '100vh',
        overflowY: 'auto'
    };

    return (
        <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#f9fafb' }}>
            {/* Sidebar */}
            <aside style={sidebarStyle}>
                <div style={{ paddingBottom: '2rem', borderBottom: '1px solid #e5e7eb', marginBottom: '1.5rem' }}>
                    <Logo />
                </div>
                
                <nav>
                    <div style={navItemStyle(currentPage === 'dashboard')} onClick={() => onNavigate('dashboard')}>
                        <LayoutDashboard style={iconStyle} />
                        <span>Dashboard</span>
                    </div>
                    <div style={navItemStyle(currentPage === 'fleet')} onClick={() => onNavigate('fleet')}>
                        <Car style={iconStyle} />
                        <span>Flota</span>
                    </div>
                    <div style={navItemStyle(currentPage === 'map')}>
                        <Map style={iconStyle} />
                        <span>Mapa</span>
                    </div>
                    <div style={navItemStyle(currentPage === 'reports')}>
                        <FileText style={iconStyle} />
                        <span>Reportes</span>
                    </div>
                </nav>

                <div 
                    onClick={handleLogout} 
                    style={{ ...navItemStyle(), marginTop: 'auto', borderTop: '1px solid #e5e7eb', paddingTop: '1.5rem' }}
                >
                    <LogOut style={{ ...iconStyle, color: '#dc2626' }} />
                    <span style={{ color: '#dc2626' }}>Cerrar Sesión</span>
                </div>
            </aside>

            {/* Contenido Principal */}
            <main style={mainContentStyle}>
                {children}
            </main>
        </div>
    );
};

export default DashboardLayout;