export type CategoryType = 'VESTUARIO' | 'ELETRONICOS' | 'COSMETICOS' | 'PETS' | 'LIVRARIA';

export type PaymentMethodType = 'CC' | 'CD' | 'PIX' | 'DINHEIRO';

export type OrderStatusType = 
  | 'ESPERANDO'
  | 'ACEITO'
  | 'RECUSADO'
  | 'PREPARACAO'
  | 'ROTA'
  | 'CONCLUIDO'
  | 'CANCELADO';

export interface Store {
  id: string;
  name: string;
  description: string;
  image?: string;
  category: CategoryType;
  telephone: string;
}

export interface Product {
  id: string;
  name: string;
  description: string;
  image?: string;
  quantity: number;
  price: number;
  inserted_at: string;
  updated_at: string;
}

export interface OrderItem {
  id: string;
  name: string;
  description?: string;
  image?: string;
  price: number;
  quantity?: number;
  inserted_at?: string;
  updated_at?: string;
}

export interface Address {
  id: string;
  address_number: string;
  street: string;
  neighborhood: string;
  cep: string;
}

export interface Account {
  id: string;
  username: string;
  email: string;
  addresses?: Address[];
}

export interface Order {
  id: string;
  total: string;
  inserted_at: string;
  updated_at: string;
  payment_method: PaymentMethodType;
  status_order: OrderStatusType; // Also serialized as status in some endpoints
  status?: OrderStatusType;
  customer_name?: string;
  customer_email?: string;
  customer_username?: string;
  address?: Address;
  order_product?: OrderItem[];
}

export type SortField = 'id' | 'inserted_at' | 'updated_at' | 'customer_name' | 'total' | 'status_order';
export type SortDirection = 'asc' | 'desc';

export type ActiveTab = 'orders' | 'inventory' | 'store_profile';
