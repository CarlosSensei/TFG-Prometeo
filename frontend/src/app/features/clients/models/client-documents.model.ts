
export type QuoteStatus =
  | 'DRAFT'
  | 'SENT'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'EXPIRED'
  | 'CONVERTED';

export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'READY'
  | 'DELIVERED'
  | 'CANCELLED';

export interface ClientQuote {
  id: number;
  quoteNumber: string;
  clientId: number;
  status: QuoteStatus;
  quoteDate: string;
  expirationDate: string | null;
  total: number;
  notes: string | null;
}

export interface ClientOrder {
  id: number;
  orderNumber: string;
  clientId: number;
  quoteId: number | null;
  status: OrderStatus;
  orderDate: string;
  expectedDeliveryDate: string | null;
  deliveryDate: string | null;
  total: number;
  notes: string | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
