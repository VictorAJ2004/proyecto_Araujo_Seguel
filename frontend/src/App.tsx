import { useEffect, useState } from 'react'
import { fetchOrders } from './api'
import type { Order } from './api'

function App() {
  const [orders, setOrders] = useState<Order[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    fetchOrders().then(setOrders).catch((reason: Error) => setError(reason.message))
  }, [])

  return <main className="shell">
    <header><span>Pedidos360</span><small>Panel protegido</small></header>
    <section className="hero"><p>OPERACIONES</p><h1>Pedidos bajo control.</h1><span>{orders.length} pedidos registrados</span></section>
    <section className="panel"><div className="panel-title"><h2>Pedidos recientes</h2><span>API Gateway / Cognito</span></div>
      {error && <p className="error">{error}</p>}
      {!error && orders.length === 0 && <p>Aún no hay pedidos o el backend no está conectado.</p>}
      {orders.map((order) => <article className="order" key={order.id}><div><strong>{order.customer}</strong><small>{order.email}</small></div><b>{(order.totalCents / 100).toFixed(2)} €</b><em>{order.status}</em></article>)}
    </section>
  </main>
}

export default App
