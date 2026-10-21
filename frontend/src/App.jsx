import { useState, useEffect } from 'react';
import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css';
import './amplify';
import { apiFetch } from './api';
import './App.css'; // Conectamos la hoja de estilos

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
        body: JSON.stringify({ name: nuevoNombre, price: parseFloat(nuevoPrecio) })
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
        <div className="app-container">
          <header className="header">
            <h2>Pedidos360</h2>
            <button onClick={signOut} className="btn-logout">
              Cerrar sesión ({user?.signInDetails?.loginId})
            </button>
          </header>

          <main className="main-content">
            {/* Formulario de Creación */}
            <div className="admin-card">
              <h3>Gestión de Catálogo (Admin)</h3>
              <form onSubmit={agregarProducto} className="form-row">
                <input 
                  type="text" 
                  placeholder="Nombre del producto" 
                  value={nuevoNombre} 
                  onChange={(e) => setNuevoNombre(e.target.value)} 
                  required 
                  className="input-field"
                />
                <input 
                  type="number" 
                  placeholder="Precio ($)" 
                  value={nuevoPrecio} 
                  onChange={(e) => setNuevoPrecio(e.target.value)} 
                  required 
                  className="input-field"
                />
                <button type="submit" className="btn-crear">+ Agregar Producto</button>
              </form>
            </div>

            {/* Lista de Productos en Tarjetas */}
            <ul className="products-grid">
              {productos.map(p => (
                <li key={p.id} className="product-card">
                  <div className="product-info">
                    <h4>{p.name}</h4>
                    {/* Formatea el precio con separador de miles */}
                    <p className="product-price">${p.price.toLocaleString('es-CL')}</p>
                  </div>
                  <button onClick={() => eliminarProducto(p.id)} className="btn-eliminar">
                    Eliminar
                  </button>
                </li>
              ))}
            </ul>
          </main>
        </div>
      )}
    </Authenticator>
  );
}

export default App;