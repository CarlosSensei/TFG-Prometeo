import { QuoteLineDraft, LineTotal } from '../models/quote-line-draft.model';

export function roundMoney(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}

export function calculateLine(line: QuoteLineDraft): LineTotal {
  const gross = line.inventoryItem.salePrice * line.quantity;

  const subtotal = roundMoney(Math.max(0, gross - line.discount));

  const tax = roundMoney((subtotal * line.inventoryItem.taxRate) / 100);

  return {
    subtotal,
    tax,
    total: roundMoney(subtotal + tax),
  };
}
