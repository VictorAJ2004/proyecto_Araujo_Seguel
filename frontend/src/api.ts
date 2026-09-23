import { fetchAuthSession } from 'aws-amplify/auth';

export const apiFetch = async (endpoint: string, options: RequestInit = {}) => {
    try {
        const session = await fetchAuthSession();
        
        // ESTO ES LO NUEVO: Imprimimos los grupos en la consola para ver la verdad
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

        if (!response.ok) {
            throw new Error(`Error ${response.status}: ${response.statusText}`);
        }

        return response.json();
    } catch (error) {
        console.error("Error en la petición al backend:", error);
        throw error;
    }
};