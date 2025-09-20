import { db } from '../config/firebase';
import { 
    doc, 
    setDoc, 
    collection, 
    addDoc, 
    getDocs, 
    query, 
    where, 
    updateDoc, 
    deleteDoc,
    writeBatch
} from "firebase/firestore";
import { createUserWithEmailAndPassword } from "firebase/auth";
import { auth } from '../config/firebase';


/**
 * Guarda los datos de la encuesta de la empresa en la colección 'companies'.
 * Asigna el ID del documento al UID del usuario para una fácil consulta.
 * También actualiza el perfil del usuario para asignarle un rol y la referencia a su empresa.
 * @param {string} userId El UID del usuario autenticado.
 * @param {object} companyData Los datos de la empresa de la encuesta.
 */
export const saveCompanyData = async (userId, companyData) => {
    try {
        // Guardar los datos de la empresa en la colección 'companies'
        const companyRef = doc(db, 'companies', userId);
        await setDoc(companyRef, {
            name: companyData.companyName,
            nit: companyData.nit,
            type: companyData.companyType,
            drivers: Number(companyData.drivers),
            vehicles: Number(companyData.vehicles)
        });
        
        // Actualizar el documento del usuario en la colección 'users'
        const userRef = doc(db, 'users', userId);
        await setDoc(userRef, {
            companyId: userId,
            role: 'admin'
        }, { merge: true });

        console.log("Datos de la empresa y usuario guardados correctamente!");
    } catch (error) {
        console.error("Error al guardar los datos de la empresa: ", error);
        throw error;
    }
};

/**
 * Crea un nuevo usuario (conductor) y su perfil en Firestore.
 * @param {string} companyId El ID de la empresa del admin logeado.
 * @param {object} driverData Los datos del conductor (nombre y email).
 */
export const createDriver = async (companyId, driverData) => {
    try {
        // Crear el usuario con autenticación de Firebase
        const userCredential = await createUserWithEmailAndPassword(auth, driverData.email, 'passwordTemporal123');
        const userId = userCredential.user.uid;
        
        // Guardar el perfil del conductor en la colección 'users'
        const userRef = doc(db, 'users', userId);
        await setDoc(userRef, {
            name: driverData.name,
            email: driverData.email,
            role: 'driver',
            companyId: companyId,
            vehicleId: null
        });
        console.log("Conductor creado y perfil guardado con ID:", userId);
        return { success: true };
    } catch (error) {
        console.error("Error al crear conductor:", error);
        throw error;
    }
};

/**
 * Lee y devuelve todos los conductores de una empresa específica.
 * @param {string} companyId El ID de la empresa a consultar.
 * @returns {Array} Una lista de objetos de conductores.
 */
export const getDrivers = async (companyId) => {
    try {
        const drivers = [];
        const q = query(collection(db, 'users'), where('companyId', '==', companyId), where('role', '==', 'driver'));
        const querySnapshot = await getDocs(q);
        querySnapshot.forEach((doc) => {
            drivers.push({ id: doc.id, ...doc.data() });
        });
        console.log("Conductores cargados:", drivers);
        return drivers;
    } catch (error) {
        console.error("Error al obtener conductores:", error);
        return [];
    }
};

/**
 * Obtiene los conductores que no tienen un vehículo asignado.
 * @param {string} companyId El ID de la empresa a consultar.
 * @returns {Array} Una lista de objetos de conductores.
 */
export const getAvailableDrivers = async (companyId) => {
    try {
        const drivers = [];
        const q = query(
            collection(db, 'users'), 
            where('companyId', '==', companyId), 
            where('role', '==', 'driver'),
            where('vehicleId', '==', null) // Filtrar por conductores sin vehículo
        );
        const querySnapshot = await getDocs(q);
        querySnapshot.forEach((doc) => {
            drivers.push({ id: doc.id, ...doc.data() });
        });
        console.log("Conductores disponibles cargados:", drivers);
        return drivers;
    } catch (error) {
        console.error("Error al obtener conductores disponibles:", error);
        return [];
    }
};


/**
 * Actualiza los datos de un conductor.
 * @param {string} driverId El ID del conductor a actualizar.
 * @param {object} updatedData Los datos a actualizar.
 */
export const updateDriver = async (driverId, updatedData) => {
    try {
        const driverRef = doc(db, 'users', driverId);
        await updateDoc(driverRef, updatedData);
        console.log("Conductor actualizado con ID:", driverId);
    } catch (error) {
        console.error("Error al actualizar conductor:", error);
        throw error;
    }
};

/**
 * Elimina un conductor de la colección 'users'.
 * @param {string} driverId El ID del conductor a eliminar.
 */
export const deleteDriver = async (driverId) => {
    try {
        const driverRef = doc(db, 'users', driverId);
        await deleteDoc(driverRef);
        console.log("Conductor eliminado con ID:", driverId);
    } catch (error) {
        console.error("Error al eliminar conductor:", error);
        throw error;
    }
};

/**
 * Crea un nuevo vehículo para una empresa.
 * @param {string} companyId El ID de la empresa.
 * @param {object} vehicleData Los datos del vehículo.
 */
export const createVehicle = async (companyId, vehicleData) => {
    try {
        const docRef = await addDoc(collection(db, 'vehicles'), {
            ...vehicleData,
            companyId: companyId,
            status: 'active', // Estado por defecto
            driverId: null, // Sin conductor asignado al crear
            routeId: null,  // Sin ruta asignada al crear
            location: null, // Sin ubicación al crear
        });
        console.log("Vehículo creado con ID: ", docRef.id);
        return { success: true };
    } catch (e) {
        console.error("Error al añadir vehículo: ", e);
        throw e;
    }
};

/**
 * Obtiene todos los vehículos de una empresa.
 * @param {string} companyId El ID de la empresa.
 * @returns {Array} Una lista de objetos de vehículos.
 */
export const getVehicles = async (companyId) => {
    try {
        const vehicles = [];
        const q = query(collection(db, 'vehicles'), where('companyId', '==', companyId));
        const querySnapshot = await getDocs(q);
        querySnapshot.forEach((doc) => {
            vehicles.push({ id: doc.id, ...doc.data() });
        });
        console.log("Vehículos cargados:", vehicles);
        return vehicles;
    } catch (e) {
        console.error("Error al obtener vehículos: ", e);
        return [];
    }
};

/**
 * Actualiza los datos de un vehículo.
 * @param {string} vehicleId El ID del vehículo a actualizar.
 * @param {object} updatedData Los datos a actualizar.
 */
export const updateVehicle = async (vehicleId, updatedData) => {
    try {
        const vehicleRef = doc(db, 'vehicles', vehicleId);
        await updateDoc(vehicleRef, updatedData);
        console.log("Vehículo actualizado con ID:", vehicleId);
    } catch (e) {
        console.error("Error al actualizar vehículo: ", e);
        throw e;
    }
};

/**
 * Elimina un vehículo.
 * @param {string} vehicleId El ID del vehículo a eliminar.
 */
export const deleteVehicle = async (vehicleId) => {
    try {
        await deleteDoc(doc(db, 'vehicles', vehicleId));
        console.log("Vehículo eliminado con ID:", vehicleId);
    } catch (e) {
        console.error("Error al eliminar vehículo: ", e);
        throw e;
    }
};

/**
 * Asigna un conductor a un vehículo y viceversa usando una transacción por lotes.
 * @param {string} vehicleId El ID del vehículo a actualizar.
 * @param {string} driverId El ID del conductor a asignar. Si es null, el vehículo queda sin conductor.
 * @param {string} oldDriverId El ID del conductor previamente asignado, si lo hay.
 */
export const assignDriverToVehicle = async (vehicleId, driverId, oldDriverId) => {
    const batch = writeBatch(db);
    
    // 1. Desasignar el vehículo del conductor anterior si existe
    if (oldDriverId) {
        const oldDriverRef = doc(db, 'users', oldDriverId);
        batch.update(oldDriverRef, { vehicleId: null });
    }

    // 2. Asignar el vehículo al nuevo conductor
    if (driverId) {
        const newDriverRef = doc(db, 'users', driverId);
        batch.update(newDriverRef, { vehicleId: vehicleId });
    }

    // 3. Asignar el conductor al vehículo
    const vehicleRef = doc(db, 'vehicles', vehicleId);
    batch.update(vehicleRef, { driverId: driverId });
    
    try {
        await batch.commit();
        console.log("Asignación de conductor a vehículo completada.");
    } catch (e) {
        console.error("Error en la asignación: ", e);
        throw e;
    }
};

/**
 * Guarda una nueva ruta en Firestore.
 * @param {string} companyId El ID de la empresa a la que pertenece la ruta.
 * @param {string} routeName El nombre de la ruta.
 * @param {Array} routePoints Los puntos de la polilínea.
 */
export const saveRoute = async (companyId, routeName, routePoints) => {
    try {
        const docRef = await addDoc(collection(db, "routes"), {
            name: routeName,
            companyId: companyId,
            points: routePoints
        });
        console.log("Ruta guardada con ID:", docRef.id);
        return { success: true, id: docRef.id };
    } catch (e) {
        console.error("Error al guardar la ruta:", e);
        throw e;
    }
};

/**
 * Obtiene todos los pagos de una empresa para el día actual.
 * @param {string} companyId El ID de la empresa.
 * @returns {Array} Una lista de objetos de pagos.
 */
export const getDailyPayments = async (companyId) => {
    try {
        const startOfToday = new Date();
        startOfToday.setHours(0, 0, 0, 0);
        const payments = [];
        const q = query(
            collection(db, 'payments'), 
            where('companyId', '==', companyId), 
            where('timestamp', '>=', startOfToday)
        );
        const querySnapshot = await getDocs(q);
        querySnapshot.forEach((doc) => {
            payments.push({ id: doc.id, ...doc.data() });
        });
        console.log("Pagos del día cargados:", payments);
        return payments;
    } catch (e) {
        console.error("Error al obtener los pagos del día: ", e);
        return [];
    }
};
/*La función createDriver crea una contraseña temporal.*/