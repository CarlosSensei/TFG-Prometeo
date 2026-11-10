export type StockMovementType = 'ENTRY' | 'EXIT' | 'ADJUSTMENT';

export interface CreateStockMovement {
  type: StockMovementType;
  quantity: number;
  reason: string;
}

export interface StockMovement {
  id: number;
  inventoryItemId: number;
  userId: number;
  userName: string;
  type: StockMovementType;
  previousStock: number;
  newStock: number;
  delta: number;
  reason: string;
  createdAt: string;
}

export interface StockMovementPage {
  content: StockMovement[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
