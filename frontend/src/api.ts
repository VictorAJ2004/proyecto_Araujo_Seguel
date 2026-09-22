import { fetchAuthSession } from 'aws-amplify/auth';

export const apiFetch = async (endpoint: string, options: RequestInit = {}) => {
    try {
        // Extrae la sesión actual
        const session = await fetchAuthSession();
        // CAMBIO CLAVE: Usamos idToken en lugar de accessToken
        const token = session.tokens?.idToken?.toString();

        // Prepara las cabeceras inyectando el token
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const baseUrl = 'http://localhost:8081'; 
        const response = await fetch(`${baseUrl}${endpoint}`, {
            ...options,
            headers
        });

        if (!response.ok) {
            throw new Error(`Error ${response.status}: ${response.statusText}`);
        }

        return response.json();
    } catch (error) {
        console.error("Error en la petición al backend:", error);
        throw error;
    }
};