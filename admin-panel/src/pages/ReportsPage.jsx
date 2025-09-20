import React, { useState, useEffect } from 'react';
import { getAuth } from 'firebase/auth';
import { getDailyPayments } from '../services/firestoreApi';

const ReportsPage = () => {
    const [dailyReport, setDailyReport] = useState({});
    const [loading, setLoading] = useState(false);
    const auth = getAuth();
    const companyId = auth.currentUser ? auth.currentUser.uid : null;
    
    useEffect(() => {
        const fetchReport = async () => {
            if (!companyId) return;
            setLoading(true);
            try {
                const payments = await getDailyPayments(companyId);
                
                const aggregatedReport = payments.reduce((acc, payment) => {
                    if (acc[payment.driverId]) {
                        acc[payment.driverId].totalAmount += payment.amount;
                        acc[payment.driverId].paymentCount += 1;
                    } else {
                        acc[payment.driverId] = {
                            totalAmount: payment.amount,
                            paymentCount: 1,
                            driverId: payment.driverId
                        };
                    }
                    return acc;
                }, {});
                
                setDailyReport(aggregatedReport);
            } catch (error) {
                console.error("Error al generar el reporte:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchReport();
    }, [companyId]);
    
    const tableHeaderStyles = {
        backgroundColor: '#f9fafb',
        color: '#4b5563',
        fontWeight: '600',
        textTransform: 'uppercase',
        fontSize: '0.75rem',
        padding: '0.75rem 1.5rem',
        textAlign: 'left'
    };

    const tableRowStyles = {
        borderBottom: '1px solid #e5e7eb'
    };

    const tableCellStyles = {
        padding: '1rem 1.5rem',
        color: '#4b5563',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis'
    };

    return (
        <div style={{ padding: '1.5rem', backgroundColor: 'white', borderRadius: '1rem', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)', border: '1px solid #f3f4f6' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h1 style={{ fontSize: '1.875rem', fontWeight: '700', color: '#111827', margin: 0 }}>Reportes Financieros</h1>
            </div>

            <h2 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#111827', marginBottom: '1rem' }}>Ingresos del Día Actual</h2>
            
            {loading ? (
                <p style={{ textAlign: 'center', color: '#6b7280' }}>Cargando reportes...</p>
            ) : (
                <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr>
                                <th style={tableHeaderStyles}>ID del Conductor</th>
                                <th style={tableHeaderStyles}>Total Recaudado</th>
                                <th style={tableHeaderStyles}>No. de Pagos</th>
                            </tr>
                        </thead>
                        <tbody>
                            {Object.keys(dailyReport).length > 0 ? (
                                Object.keys(dailyReport).map(driverId => (
                                    <tr key={driverId} style={tableRowStyles}>
                                        <td style={tableCellStyles}>{driverId}</td>
                                        <td style={tableCellStyles}>
                                            ${dailyReport[driverId].totalAmount.toLocaleString('es-CO')}
                                        </td>
                                        <td style={tableCellStyles}>{dailyReport[driverId].paymentCount}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr style={tableRowStyles}>
                                    <td style={tableCellStyles} colSpan="3">
                                        <p style={{ textAlign: 'center', color: '#6b7280' }}>
                                            No se han registrado pagos hoy.
                                        </p>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default ReportsPage;