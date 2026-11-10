import { Page } from '../../clients/models/page.model';

export type InventoryType = 'FRAME' | 'LENS' | 'CONTACT_LENS' | 'SUNGLASSES' | 'ACCESSORY' | 'SERVICE';

export interface InventoryItem {
  id: number;
  sku: string;
  name: string;
  type: InventoryType;
  brand: string | null;
  description: string | null;
  salePrice: number;
  taxRate: number;
  stock: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface InventoryPayload {
  sku: string;
  name: string;
  type: InventoryType;
  brand: string | null;
  description: string | null;
  salePrice: number;
  taxRate: number;
  stock: number;
}

export interface InventorySearch {
  sku?: string;
  name?: string;
  brand?: string;
  types?: InventoryType[];
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  active: boolean;
  page: number;
  size: number;
  sortBy: string;
  sortDirection: 'asc' | 'desc';
}

export type InventoryPage = Page<InventoryItem>;

export const INVENTORY_TYPES: {value: InventoryType; label: string}[] = [
  {value: 'FRAME', label: 'Montura'},
  {value: 'LENS', label: 'Lente'},
  {value: 'CONTACT_LENS', label: 'Lentilla'},
  {value: 'SUNGLASSES', label: 'Gafas de sol'},
  {value: 'ACCESSORY', label: 'Accesorio'},
  {value: 'SERVICE', label: 'Servicio'},
];
