// src/config/firebase.js
import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

// Configuración directa (temporal para desarrollo)
const firebaseConfig = {
  apiKey: "AIzaSyDPnz38MkxkeExovNAL-Eg2FyE1_PRfmqw",
  authDomain: "villavo-conecta.firebaseapp.com",
  projectId: "villavo-conecta",
  storageBucket: "villavo-conecta.firebasestorage.app", 
  messagingSenderId: "1099063898387",
  appId: "1:1099063898387:web:ca93b6263ee44997efeae6",
  measurementId:"G-0EJ6LM5YKC"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export default app;