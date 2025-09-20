import React, { useState, useEffect } from 'react';
import { getAuth } from 'firebase/auth';
import { collection, query, where, onSnapshot } from 'firebase/firestore';
import { db } from '../config/firebase';

const DashboardPage = () => {
    const [vehicles, setVehicles] = useState([]);
    const [drivers, setDrivers] = useState([]);
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const auth = getAuth();
    const companyId = auth.currentUser ? auth.currentUser.uid : null;

    useEffect(() => {
        if (!companyId) {
            setLoading(false);
            return;
        }

        const fetchAllData = async () => {
            // Lectura en tiempo real de vehículos
            const vehiclesQuery = query(collection(db, 'vehicles'), where('companyId', '==', companyId));
            const unsubscribeVehicles = onSnapshot(vehiclesQuery, (querySnapshot) => {
                const fetchedVehicles = querySnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
                setVehicles(fetchedVehicles);
                console.log("Vehículos cargados:", fetchedVehicles);
            });

            // Lectura en tiempo real de conductores
            const driversQuery = query(collection(db, 'users'), where('companyId', '==', companyId), where('role', '==', 'driver'));
            const unsubscribeDrivers = onSnapshot(driversQuery, (querySnapshot) => {
                const fetchedDrivers = querySnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
                setDrivers(fetchedDrivers);
                console.log("Conductores cargados:", fetchedDrivers);
            });

            // Lectura en tiempo real de pagos del día
            const startOfToday = new Date();
            startOfToday.setHours(0, 0, 0, 0);
            const paymentsQuery = query(
                collection(db, 'payments'),
                where('companyId', '==', companyId),
                where('timestamp', '>=', startOfToday)
            );
            const unsubscribePayments = onSnapshot(paymentsQuery, (querySnapshot) => {
                const fetchedPayments = querySnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
                setPayments(fetchedPayments);
                console.log("Pagos del día cargados:", fetchedPayments);
                setLoading(false);
            });

            return () => {
                unsubscribeVehicles();
                unsubscribeDrivers();
                unsubscribePayments();
            };
        };

        fetchAllData();
    }, [companyId]);

    const activeDrivers = drivers.filter(d => d.status === 'active');
    const activeVehicles = vehicles.filter(v => v.status === 'active');
    const totalRevenueToday = payments.reduce((sum, payment) => sum + (payment.amount || 0), 0);

    const cardStyle = {
        backgroundColor: 'white',
        padding: '1.5rem',
        borderRadius: '1rem',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        border: '1px solid #f3f4f6',
        textAlign: 'center',
        flex: 1,
        margin: '0 0.5rem',
        minWidth: '200px'
    };

    const cardTitleStyle = {
        color: '#4b5563',
        fontSize: '0.875rem',
        textTransform: 'uppercase',
        fontWeight: '600',
        marginBottom: '0.5rem'
    };

    const cardValueStyle = {
        color: '#111827',
        fontSize: '2.25rem',
        fontWeight: '700'
    };

    const dashboardContainerStyle = {
        padding: '1.5rem',
        backgroundColor: '#f9fafb',
        borderRadius: '1rem',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        border: '1px solid #f3f4f6'
    };

    if (loading) {
        return <div style={dashboardContainerStyle}><p>Cargando datos del dashboard...</p></div>;
    }

    return (
        <div style={dashboardContainerStyle}>
            <h1 style={{ fontSize: '1.875rem', fontWeight: '700', color: '#111827', margin: '0 0 1.5rem 0' }}>Dashboard Principal</h1>
            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', justifyContent: 'center' }}>
                <div style={cardStyle}>
                    <p style={cardTitleStyle}>Conductores Activos</p>
                    <h2 style={cardValueStyle}>{activeDrivers.length}</h2>
                </div>
                <div style={cardStyle}>
                    <p style={cardTitleStyle}>Vehículos Totales</p>
                    <h2 style={cardValueStyle}>{vehicles.length}</h2>
                </div>
                <div style={cardStyle}>
                    <p style={cardTitleStyle}>Ingresos del Día</p>
                    <h2 style={cardValueStyle}>${totalRevenueToday.toLocaleString('es-CO')}</h2>
                </div>
            </div>
        </div>
    );
};

export default DashboardPage;