export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'READY'
  | 'DELIVERED'
  | 'CANCELLED';

export interface OrderItem {
  id: number;
  inventoryItemId: number;
  quantity: number;
  description: string;
  unitPrice: number;
  discount: number;
  taxRate: number;
  subtotal: number;
  taxAmount: number;
  total: number;
}

export interface Order {
  id: number;
  orderNumber: string;

  clientId: number;
  sellerId: number | null;
  quoteId: number | null;

  status: OrderStatus;

  orderDate: string;
  expectedDeliveryDate: string | null;
  deliveryDate: string | null;

  items?: OrderItem[] | null;

  subtotal: number;
  discount: number;
  taxRate: number;
  taxAmount: number;
  total: number;

  notes: string | null;
  active: boolean;

  createdAt?: string;
  updatedAt?: string;
}

export interface OrderSearch {
  orderNumber?: string;

  clientId?: number;
  sellerId?: number;
  quoteId?: number;

  status?: OrderStatus;
  statuses?: OrderStatus[];

  orderDateFrom?: string;
  orderDateTo?: string;

  expectedDeliveryDateFrom?: string;
  expectedDeliveryDateTo?: string;

  deliveryDateFrom?: string;
  deliveryDateTo?: string;

  minTotal?: number;
  maxTotal?: number;

  active: boolean;

  page: number;
  size: number;

  sortBy: string;
  sortDirection: 'asc' | 'desc';
}

export interface OrderPage {
  content: Order[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface OrderItemPayload {
  inventoryItemId: number;
  quantity: number;
  discount: number;
}

export interface OrderPayload {
  clientId: number;
  orderDate: string;
  expectedDeliveryDate: string | null;
  notes: string | null;
  items: OrderItemPayload[];
}
