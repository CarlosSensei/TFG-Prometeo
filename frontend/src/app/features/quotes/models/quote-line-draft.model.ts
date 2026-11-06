import { InventoryItem } from '../services/quote-lookup.service';

export interface QuoteLineDraft {
  key: number;
  id?: number;
  inventoryItem: InventoryItem;
  quantity: number;
  discount: number;
}

export interface LineTotal {
  subtotal: number;
  tax: number;
  total: number;
}

export interface QuoteTotalsValue {
  subtotal: number;
  discount: number;
  tax: number;
  total: number;
}
