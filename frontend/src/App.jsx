import { useState, useEffect } from 'react';
import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css';
import './amplify';
import { apiFetch } from './api';

function App() {
  const [productos, setProductos] = useState([]);
  const [nuevoNombre, setNuevoNombre] = useState('');
  const [nuevoPrecio, setNuevoPrecio] = useState('');

  const cargarProductos = async () => {
    try {
      const data = await apiFetch('/api/products');
      setProductos(data);
    } catch (error) {
      console.error("Error cargando productos:", error);
    }
  };

  useEffect(() => {
    cargarProductos();
  }, []);

  const agregarProducto = async (e) => {
    e.preventDefault();
    try {
      await apiFetch('/api/products', {
        method: 'POST',
        body: JSON.stringify({ nombre: nuevoNombre, precio: parseFloat(nuevoPrecio) })
      });
      alert("Producto agregado (Acción de ADMIN exitosa)");
      setNuevoNombre('');
      setNuevoPrecio('');
      cargarProductos();
    } catch (error) {
      if (error.message.includes("403")) alert("Acceso Denegado (403): Solo los Administradores pueden crear productos.");
    }
  };

  const eliminarProducto = async (id) => {
    try {
      await apiFetch(`/api/products/${id}`, { method: 'DELETE' });
      alert("Producto eliminado (Acción de ADMIN exitosa)");
      cargarProductos();
    } catch (error) {
      if (error.message.includes("403")) alert("Acceso Denegado (403): Solo los Administradores pueden eliminar productos.");
    }
  };

  return (
    <Authenticator>
      {({ signOut, user }) => (
        <main style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem', fontFamily: 'sans-serif' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2>Catálogo de Productos</h2>
            <button onClick={signOut} style={{ padding: '8px', cursor: 'pointer' }}>Cerrar sesión ({user?.signInDetails?.loginId})</button>
          </div>

          {/* Formulario de Creación */}
          <form onSubmit={agregarProducto} style={{ margin: '20px 0', padding: '15px', border: '1px solid #ccc', borderRadius: '5px' }}>
            <h3>Agregar Nuevo Producto (Solo Admin)</h3>
            <input type="text" placeholder="Nombre" value={nuevoNombre} onChange={(e) => setNuevoNombre(e.target.value)} required style={{ marginRight: '10px', padding: '5px' }}/>
            <input type="number" placeholder="Precio" value={nuevoPrecio} onChange={(e) => setNuevoPrecio(e.target.value)} required style={{ marginRight: '10px', padding: '5px' }}/>
            <button type="submit" style={{ padding: '6px 12px', backgroundColor: '#4CAF50', color: 'white', border: 'none', cursor: 'pointer' }}>Crear</button>
          </form>

          {/* Lista de Productos */}
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {productos.map(p => (
              <li key={p.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px', borderBottom: '1px solid #eee' }}>
                <span><strong>{p.nombre}</strong> - ${p.precio}</span>
                <button onClick={() => eliminarProducto(p.id)} style={{ backgroundColor: '#f44336', color: 'white', border: 'none', padding: '5px 10px', cursor: 'pointer' }}>
                  Eliminar
                </button>
              </li>
            ))}
          </ul>
        </main>
      )}
    </Authenticator>
  );
}

export default App;