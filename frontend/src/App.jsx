import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css';
import './amplify';
import { apiFetch } from './api';

function App() {
  
  const probarLectura = async () => {
    try {
      const data = await apiFetch('/api/orders'); 
      alert("¡Lectura exitosa (GET)! Tienes permisos para ver esto.");
      console.log(data);
    } catch (error) {
      alert("Error en lectura. Revisa la consola.");
    }
  };

  const probarEscrituraAdmin = async () => {
    try {
      const data = await apiFetch('/api/orders', { method: 'POST' }); 
      alert("¡Acción de Administrador exitosa (POST)!");
      console.log(data);
    } catch (error) {
      const mensaje = error.message || "";
      
      // Si el backend te rechaza por no tener rol de ADMIN
      if (mensaje.includes("403")) {
        alert("Seguridad funcionando: Acceso Denegado (403). Tu usuario es CLIENTE y no puede crear o modificar pedidos.");
      } 
      // Si el backend te deja pasar porque eres ADMIN (el error 400 es normal aquí porque no enviamos datos)
      else if (mensaje.includes("400") && mensaje.includes("Bad Request")) {
        alert("¡Éxito! Eres ADMIN y el backend te autorizó a realizar acciones.");
      } 
      else {
        console.error(error);
        alert("Ocurrió un error en la conexión al backend. Revisa la consola.");
      }
    }
  };

  return (
    <Authenticator>
      {({ signOut, user }) => (
        <main style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
          <h1>Bienvenido a Pedidos360</h1>
          <p>Has iniciado sesión como: <strong>{user?.signInDetails?.loginId}</strong></p>
          
          <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
            <button onClick={probarLectura} style={{ padding: '8px 16px', cursor: 'pointer', backgroundColor: '#4CAF50', color: 'white', border: 'none' }}>
              Probar Lectura (CLIENTE/ADMIN)
            </button>
            <button onClick={probarEscrituraAdmin} style={{ padding: '8px 16px', cursor: 'pointer', backgroundColor: '#f44336', color: 'white', border: 'none' }}>
              Probar Creación (Solo ADMIN)
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