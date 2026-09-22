import { fetchAuthSession } from 'aws-amplify/auth'

export type Order = { id: number; customer: string; email: string; totalCents: number; status: string }
const apiUrl = import.meta.env.VITE_API_URL ?? 'http://localhost:8081'

export async function apiFetch(path: string, init: RequestInit = {}) {
  const session = await fetchAuthSession()
  const token = session.tokens?.accessToken?.toString()
  return fetch(`${apiUrl}${path}`, { ...init, headers: { ...init.headers, ...(token ? { Authorization: `Bearer ${token}` } : {}) } })
}

export async function fetchOrders(): Promise<Order[]> {
  const response = await apiFetch('/api/orders')
  if (!response.ok) throw new Error(`No se pudieron cargar los pedidos (${response.status})`)
  return response.json()
}
