import React, { useState, useEffect } from 'react';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import SurveyPage from './pages/SurveyPage';
import DashboardPage from './pages/DashboardPage';
import FleetPage from './pages/FleetPage';
import AuthLayout from './components/common/AuthLayout';
import DashboardLayout from './components/common/DashboardLayout';
import Logo from './components/common/Logo';
import { loginUser, registerUser } from "./services/authService.js";
import { onAuthStateChanged, getAuth } from "firebase/auth";
import { doc, getDoc } from "firebase/firestore";
import { db } from './config/firebase';
import { saveCompanyData } from './services/firestoreApi';

function App() {
  const [isLogin, setIsLogin] = useState(true);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [authError, setAuthError] = useState(null);
  const [needsOnboarding, setNeedsOnboarding] = useState(false);
  const [currentPage, setCurrentPage] = useState('dashboard');

  // Escuchar cambios en el estado de autenticación
  useEffect(() => {
    const auth = getAuth();
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        // Usuario autenticado, ahora verificamos si ya completó la encuesta
        setUser({
          uid: firebaseUser.uid,
          email: firebaseUser.email
        });
        
        const onboardingStatus = await checkOnboardingStatus(firebaseUser.uid);
        setNeedsOnboarding(!onboardingStatus);
        
      } else {
        // Usuario no autenticado
        setUser(null);
        setNeedsOnboarding(false);
      }
      setLoading(false);
    });

    // Cleanup
    return () => unsubscribe();
  }, []);

  const checkOnboardingStatus = async (userId) => {
    try {
        const companyRef = doc(db, 'companies', userId);
        const companySnap = await getDoc(companyRef);
        return companySnap.exists();
    } catch (error) {
        console.error("Error al verificar estado de la encuesta:", error);
        return false;
    }
  };

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
      try {
          setLoading(true);
          await saveCompanyData(user.uid, formData);
          setNeedsOnboarding(false);
      } catch (error) {
          console.error('Error al enviar la encuesta:', error);
          alert('Hubo un error al guardar los datos. Por favor, inténtalo de nuevo.');
      } finally {
          setLoading(false);
      }
  };

  const toggleMode = () => {
    setIsLogin(!isLogin);
    setAuthError(null);
  };

  const renderPage = () => {
        switch (currentPage) {
            case 'dashboard':
                return <DashboardPage />;
            case 'fleet':
                return <FleetPage />;
            case 'map':
                return <Map />;
            case 'reports':
                return <ReportsPage />;
            default:
                return <DashboardPage />;
        }
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
            <SurveyPage onSubmit={handleSurveySubmit} loading={loading} />
          </div>
        </AuthLayout>
      );
    }
    
    // Dashboard principal (temporal)
    return (
        <DashboardLayout onNavigate={setCurrentPage} currentPage={currentPage}>
            {renderPage()}
        </DashboardLayout>
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