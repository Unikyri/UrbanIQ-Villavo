import React, { useState, useEffect } from 'react';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import SurveyPage from './pages/SurveyPage';
import Logo from './components/common/Logo';
import AuthLayout from './components/common/AuthLayout';
import { loginUser, registerUser } from "./services/authService.js";
import { onAuthStateChanged, getAuth } from "firebase/auth";

function App() {
  const [isLogin, setIsLogin] = useState(true);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [authError, setAuthError] = useState(null);
  const [needsOnboarding, setNeedsOnboarding] = useState(false);

  // Escuchar cambios en el estado de autenticación
  useEffect(() => {
    const auth = getAuth();
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        // Usuario autenticado
        setUser({
          uid: firebaseUser.uid,
          email: firebaseUser.email
        });
        console.log('Usuario autenticado:', firebaseUser.email);

        // TODO: Implementar lógica de verificación de encuesta (Tarea 1.6)
        // Por ahora, asumimos que siempre necesita la encuesta después del login
        setNeedsOnboarding(true);

      } else {
        // Usuario no autenticado
        setUser(null);
        setNeedsOnboarding(false);
        console.log('Usuario no autenticado');
      }
      setLoading(false);
    });

    // Cleanup
    return () => unsubscribe();
  }, []);

  const handleLogin = async (formData) => {
    try {
      setAuthError(null);
      await loginUser(formData.email, formData.password);
      
    } catch (error) {
      console.error('Error en handleLogin:', error);
      setAuthError(error.message);
      throw error;
    }
  };

  const handleRegister = async (formData) => {
    try {
      setAuthError(null);
      await registerUser(formData.email, formData.password);
      
    } catch (error) {
      console.error('Error en handleRegister:', error);
      setAuthError(error.message);
      throw error;
    }
  };
  
  const handleSurveySubmit = async (formData) => {
      // TODO: Implementar lógica para guardar la encuesta en Firestore (Tarea 1.6)
      console.log('Datos de la encuesta enviados:', formData);
      setNeedsOnboarding(false); // Simula que la encuesta ya fue completada
  };

  const handleLogout = async () => {
    try {
      const auth = getAuth();
      await auth.signOut();
      alert('Sesión cerrada exitosamente');
    } catch (error) {
      console.error('Error al cerrar sesión:', error);
    }
  };

  const toggleMode = () => {
    setIsLogin(!isLogin);
    setAuthError(null);
  };

  if (loading) {
    return (
      <AuthLayout>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '200px', gap: '1rem' }}>
          <div style={{ animation: 'spin 1s linear infinite', borderRadius: '50%', width: '2rem', height: '2rem', border: '3px solid #f3f4f6', borderTop: '3px solid #2563eb' }}></div>
          <p style={{ color: '#6b7280' }}>Cargando...</p>
        </div>
        <style jsx>{`@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }`}</style>
      </AuthLayout>
    );
  }

  // Si el usuario está autenticado, verificar si necesita la encuesta
  if (user) {
    if (needsOnboarding) {
      return (
        <AuthLayout>
          <Logo />
          <div style={{ marginTop: '2rem' }}>
            <SurveyPage onSubmit={handleSurveySubmit} />
          </div>
        </AuthLayout>
      );
    }
    
    // Dashboard principal (temporal)
    return (
      <AuthLayout>
        <div style={{ backgroundColor: 'white', borderRadius: '1rem', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)', border: '1px solid #f3f4f6', padding: '2rem', textAlign: 'center' }}>
          <h2 style={{ fontSize: '1.5rem', fontWeight: '600', color: '#111827', marginBottom: '1rem' }}>¡Bienvenido al Panel de Urbaniq! 🎉</h2>
          <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>Usuario: {user.email}</p>
          <p style={{ color: '#059669', fontWeight: '500', marginBottom: '2rem', padding: '1rem', backgroundColor: '#ecfdf5', borderRadius: '0.5rem', border: '1px solid #a7f3d0' }}>✅ Autenticación con Firebase funcionando correctamente</p>
          <p style={{ color: '#dc2626', marginBottom: '1.5rem' }}>📋 <strong>Próximo paso:</strong> Implementar encuesta de caracterización (Tarea 1.5)</p>
          <button onClick={handleLogout} style={{ padding: '0.75rem 1.5rem', backgroundColor: '#ef4444', color: 'white', border: 'none', borderRadius: '0.5rem', cursor: 'pointer', fontWeight: '500', transition: 'background-color 0.2s' }} onMouseEnter={(e) => e.target.style.backgroundColor = '#dc2626'} onMouseLeave={(e) => e.target.style.backgroundColor = '#ef4444'}>Cerrar Sesión</button>
        </div>
      </AuthLayout>
    );
  }

  // Pantalla de autenticación (Login/Registro)
  return (
    <AuthLayout>
      <Logo />
      <div style={{ marginTop: '2rem' }}>
        {authError && (
          <div style={{ backgroundColor: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '0.75rem', borderRadius: '0.5rem', marginBottom: '1rem', fontSize: '0.875rem' }}>❌ {authError}</div>
        )}
        
        {isLogin ? (
          <LoginPage 
            onToggleMode={toggleMode} 
            onSubmit={handleLogin} 
          />
        ) : (
          <RegisterPage 
            onToggleMode={toggleMode} 
            onSubmit={handleRegister} 
          />
        )}
      </div>
    </AuthLayout>
  );
}

export default App;