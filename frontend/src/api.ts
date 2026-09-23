import { fetchAuthSession } from 'aws-amplify/auth';

export const apiFetch = async (endpoint: string, options: RequestInit = {}) => {
    try {
        const session = await fetchAuthSession();
        
        // Imprimimos los grupos en la consola para ver la verdad
        const payload = session.tokens?.idToken?.payload;
        console.log("Grupos enviados por Cognito:", payload?.['cognito:groups']);

        const token = session.tokens?.idToken?.toString();

        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const baseUrl = 'https://tiv234t0l4.execute-api.us-east-1.amazonaws.com'; 
        const response = await fetch(`${baseUrl}${endpoint}`, {
            ...options,
            headers
        });

        // 1. Lanzamos el 403 exacto para que App.jsx lo atrape y muestre la alerta
        if (response.status === 403 || response.status === 401) {
            throw new Error("403");
        }

        if (!response.ok) {
            throw new Error(`Error ${response.status}: ${response.statusText}`);
        }

        // 2. Prevenimos el error "Unexpected end of JSON input" al eliminar productos
        if (options.method === 'DELETE' || response.status === 204) {
            return null;
        }

        return response.json();
    } catch (error) {
        console.error("Error en la petición al backend:", error);
        throw error;
    }
};