
export type QuoteStatus =
  | 'DRAFT'
  | 'SENT'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'EXPIRED'
  | 'CONVERTED';

export interface Quote {
  id: number;
  quoteNumber: string;
  clientId: number;
  sellerId: number | null;
  status: QuoteStatus;
  quoteDate: string;
  expirationDate: string | null;
  items?: QuoteItem[] | null;
  subtotal: number;
  discount: number;
  taxRate: number;
  taxAmount: number;
  total: number;
  notes: string | null;
  active: boolean;
}


export interface QuoteItem {
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


export interface QuoteSearch {
  // Identificación
  quoteNumber?: string;
  clientId?: number;

  // Vendedor
  sellerId?: number;
  sellerName?: string;

  // Estado
  status?: QuoteStatus;
  statuses?: QuoteStatus[];

  // Fechas del presupuesto
  quoteDateFrom?: string;
  quoteDateTo?: string;

  // Fechas de caducidad
  expirationDateFrom?: string;
  expirationDateTo?: string;

  // Importe
  minTotal?: number;
  maxTotal?: number;

  // Estado lógico
  active: boolean;

  // Paginación
  page: number;
  size: number;

  // Ordenación
  sortBy: string;
  sortDirection: 'asc' | 'desc';
}


export interface QuotePage {
  content: Quote[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface QuoteItemPayload {
  id?: number;
  inventoryItemId: number;
  quantity: number;
  discount: number;
}

export interface QuotePayload {
  clientId: number;
  status: 'DRAFT';
  quoteDate: string;
  expirationDate: string;
  notes: string | null;
  items: QuoteItemPayload[];
}
