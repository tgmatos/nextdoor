import { Socket } from 'phoenix';
import { getAuthToken } from './client';
import { Order } from '../types';

const BASE_URL = ((import.meta as any).env?.VITE_API_BASE_URL || '').replace(/\/$/, '');
const WS_URL = BASE_URL.replace(/^http/, 'ws') + '/socket';

export function getOwnerIdFromToken(): string | null {
  const token = getAuthToken();
  if (!token) return null;
  const payload = token.split('.')[1];
  if (!payload) return null;
  try {
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(normalized.length + ((4 - normalized.length % 4) % 4), '=');
    const json = atob(padded);
    const claims = JSON.parse(json);
    return typeof claims.sub === 'string' ? claims.sub : null;
  } catch {
    return null;
  }
}

function normalizeOrder(raw: any): Order {
  const st = raw.status_order || raw.status || 'ESPERANDO';
  return {
    id: raw.id,
    total: raw.total,
    inserted_at: raw.inserted_at || raw.updated_at || new Date().toISOString(),
    updated_at: raw.updated_at || raw.inserted_at || new Date().toISOString(),
    payment_method: raw.payment_method,
    status_order: st,
    status: st,
    client: raw.client,
    address: raw.address,
    order_product: raw.order_product
  };
}

export interface OrderChannelHandlers {
  onNewOrder: (order: Order) => void;
  onOrderUpdated: (order: Order) => void;
}

export function connectStoreOrdersChannel(handlers: OrderChannelHandlers): () => void {
  const ownerId = getOwnerIdFromToken();
  if (!ownerId) {
    console.warn('Store orders channel: missing owner_id (no token claims)');
    return () => {};
  }

  const socket = new Socket(WS_URL, {
    params: { token: getAuthToken() },
    heartbeatIntervalMs: 30000,
    reconnectAfterMs: tries => Math.min(1000 * Math.pow(2, tries), 15000)
  });

  socket.onError(() => {
    console.warn('Store orders socket connection error');
  });

  socket.connect();

  const channel = socket.channel(`store:order:${ownerId}`, {});
  channel.on('new_order', raw => {
    try {
      handlers.onNewOrder(normalizeOrder(raw));
    } catch (err) {
      console.error('Error handling new_order event', err);
    }
  });
  channel.on('order_updated', raw => {
    try {
      handlers.onOrderUpdated(normalizeOrder(raw));
    } catch (err) {
      console.error('Error handling order_updated event', err);
    }
  });

  channel
    .join()
    .receive('ok', () => {
      console.log('Joined store orders channel');
    })
    .receive('error', (resp) => {
      console.error('Failed to join store orders channel', resp);
    });

  return () => {
    channel.leave();
    socket.disconnect();
  };
}