import { InventoryItem } from './inventory-item.model';

export interface CommercialLineDraft {
  key: number;
  id?: number;
  inventoryItem: InventoryItem;
  quantity: number;
  discount: number;
}

export interface CommercialLineTotal {
  subtotal: number;
  tax: number;
  total: number;
}

export interface CommercialTotals {
  subtotal: number;
  discount: number;
  tax: number;
  total: number;
}
