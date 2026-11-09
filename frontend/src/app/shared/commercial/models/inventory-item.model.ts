export interface InventoryItem {
  id: number;
  name: string;
  sku: string;
  brand: string | null;
  salePrice: number;
  taxRate: number;
  stock: number;
  active: boolean;
}
