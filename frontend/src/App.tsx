import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css';
import './amplify';
import { apiFetch } from './api';

function App() {
  
  const probarConexion = async () => {
    try {
      // Intentará consumir un endpoint protegido de tu microservicio
      const data = await apiFetch('/api/orders'); 
      alert("¡Conexión exitosa con el backend protegido!");
      console.log(data);
    } catch (error) {
      alert("Error de conexión. Revisa la consola para más detalles.");
    }
  };

  return (
    <Authenticator>
      {({ signOut, user }) => (
        <main style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
          <h1>Bienvenido a Pedidos360</h1>
          <p>Has iniciado sesión como: <strong>{user?.signInDetails?.loginId}</strong></p>
          
          <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
            <button onClick={probarConexion} style={{ padding: '8px 16px', cursor: 'pointer' }}>
              Probar Backend Protegido
            </button>
            <button onClick={signOut} style={{ padding: '8px 16px', cursor: 'pointer' }}>
              Cerrar sesión
            </button>
          </div>
        </main>
      )}
    </Authenticator>
  );
}

export default App;