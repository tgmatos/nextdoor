import { Order, Product, Store, Account, Address, OrderStatusType } from '../types';

let authToken = localStorage.getItem('nextdoor_auth_token') || '';

export function setAuthToken(token: string) {
  authToken = token;
  localStorage.setItem('nextdoor_auth_token', token);
}

export function clearAuthToken() {
  authToken = '';
  localStorage.removeItem('nextdoor_auth_token');
}

export function getAuthToken() {
  return authToken;
}

const BASE_URL = ((import.meta as any).env?.VITE_API_BASE_URL || '').replace(/\/$/, '');

async function apiRequest<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${authToken}`,
    ...(options.headers as Record<string, string> || {})
  };

  const url = `${BASE_URL}${endpoint}`;

  const response = await fetch(url, {
    ...options,
    headers
  });

  if (response.status === 204) {
    return {} as T;
  }

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const errorMsg = data?.errors?.detail || data?.error || (data?.errors ? JSON.stringify(data.errors) : 'Erro na requisição');
    throw new Error(errorMsg);
  }

  return data as T;
}

export async function registerAccount(data: {
  email: string;
  username: string;
  password?: string;
  address?: { street?: string; number?: string; neighborhood?: string; cep?: string };
}): Promise<{ token: string }> {
  const payload = {
    email: data.email,
    username: data.username,
    password: data.password || "",
    address: {
      number: data.address?.number || "1",
      street: data.address?.street || "N/A",
      neighborhood: data.address?.neighborhood || "N/A",
      cep: data.address?.cep || "00000000"
    }
  };

  const res = await apiRequest<{ token: string }>('/api/account/register', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  if (res.token) {
    setAuthToken(res.token);
  }
  return res;
}

export async function loginAccount(data: {
  email: string;
  password?: string;
}): Promise<{ token: string }> {
  const payload = {
    email: data.email,
    password: data.password || ""
  };

  const res = await apiRequest<{ token: string }>('/api/account/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  if (res.token) {
    setAuthToken(res.token);
  }
  return res;
}

export async function logoutAccount(): Promise<void> {
  await apiRequest('/api/account/logout', { method: 'GET' }).catch(() => {});
}

export async function createStore(storeData: {
  name: string;
  description?: string;
  telephone?: string;
  category?: string;
  image?: string;
}): Promise<{ id: string }> {
  const payload = {
    name: storeData.name,
    description: storeData.description || "",
    telephone: storeData.telephone || "",
    category: storeData.category || "VESTUARIO",
    image: storeData.image || "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
  };
  return apiRequest<{ id: string }>('/api/store', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

// 1. Orders
export async function fetchStoreOrders(): Promise<Order[]> {
  const data = await apiRequest<{ orders: Order[] }>('/api/store/order');
  const rawOrders = data.orders || [];
  return rawOrders.map(o => {
    const st = o.status_order || (o as any).status || 'ESPERANDO';
    return {
      ...o,
      status_order: st,
      status: st
    };
  });
}

export async function fetchOrderDetails(orderId: string): Promise<Order> {
  const data = await apiRequest<Order | { order: Order }>(`/api/store/order/${orderId}`);
  const raw = ('order' in data) ? data.order : data;
  const st = raw.status_order || (raw as any).status || 'ESPERANDO';
  return {
    ...raw,
    status_order: st,
    status: st
  };
}

export async function updateOrderStatus(
  orderId: string, 
  before: OrderStatusType, 
  after: OrderStatusType
): Promise<{ id: string; status_order: OrderStatusType; total: string; payment_method: string }> {
  const res = await apiRequest<{ id: string; status_order?: OrderStatusType; status?: OrderStatusType; total: string; payment_method: string }>(
    `/api/store/order/${orderId}`,
    {
      method: 'PATCH',
      body: JSON.stringify({ before, after })
    }
  );
  const st = res.status_order || res.status || after;
  return {
    ...res,
    status_order: st
  };
}

export async function createOrder(
  storeId: string,
  products: { product: string; quantity: number }[],
  paymentMethod: string
): Promise<Order> {
  return apiRequest<Order>(`/api/store/order/${storeId}`, {
    method: 'POST',
    body: JSON.stringify({
      products,
      payment_method: paymentMethod
    })
  });
}

// 2. Inventory / Products
export async function fetchStoreProducts(): Promise<Product[]> {
  const data = await apiRequest<{ products: Product[] }>('/api/store/product');
  return data.products || [];
}

export async function createProduct(product: Partial<Product>): Promise<Product> {
  const payload = {
    product: {
      name: product.name || "",
      description: product.description || "",
      price: typeof product.price === 'number' ? product.price.toFixed(2) : (product.price || "0.00"),
      quantity: Number(product.quantity) || 0,
      image: product.image || "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
    }
  };
  const data = await apiRequest<{ product: Product }>('/api/store/product', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
  return data.product;
}

export async function updateProduct(id: string, product: Partial<Product>): Promise<Product> {
  const pPayload: Record<string, any> = {};
  if (product.name !== undefined) pPayload.name = product.name;
  if (product.description !== undefined) pPayload.description = product.description;
  if (product.price !== undefined) pPayload.price = typeof product.price === 'number' ? product.price.toFixed(2) : product.price;
  if (product.quantity !== undefined) pPayload.quantity = Number(product.quantity);
  if (product.image !== undefined) pPayload.image = product.image;

  const data = await apiRequest<{ product: Product }>(`/api/store/product/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ product: pPayload })
  });
  return data.product;
}

export async function deleteProduct(id: string): Promise<void> {
  await apiRequest(`/api/store/product/${id}`, {
    method: 'DELETE'
  });
}

// 3. Store Profile & Account
export async function fetchStoreProfile(): Promise<Store> {
  return apiRequest<Store>('/api/store');
}

export async function updateStoreProfile(store: Partial<Store>): Promise<Store> {
  return apiRequest<Store>('/api/store', {
    method: 'PATCH',
    body: JSON.stringify({ store })
  });
}

export async function deleteStoreProfile(): Promise<void> {
  await apiRequest('/api/store', {
    method: 'DELETE'
  });
}

export async function fetchAccount(): Promise<Account> {
  const data = await apiRequest<{ account: Account }>('/api/account');
  return data.account;
}

export async function updateAccount(account: Partial<Account>): Promise<{ username: string; email: string }> {
  return apiRequest<{ username: string; email: string }>('/api/account', {
    method: 'PATCH',
    body: JSON.stringify({ account })
  });
}

export async function fetchAddresses(): Promise<Address[]> {
  return apiRequest<Address[]>('/api/account/address');
}

export async function updateAddress(id: string, address: Partial<Address> & { number?: string; address_number?: string }): Promise<Address> {
  const payload = {
    address: {
      address_number: address.address_number || address.number || "1",
      street: address.street || "",
      neighborhood: address.neighborhood || "",
      cep: address.cep || ""
    }
  };
  return apiRequest<Address>(`/api/account/address/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(payload)
  });
}
