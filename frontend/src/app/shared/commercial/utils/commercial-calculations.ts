import { CommercialLineDraft, CommercialLineTotal, CommercialTotals } from '../models/commercial-line.model';

export function roundMoney(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}

export function calculateLine(line: CommercialLineDraft): CommercialLineTotal {

  const gross = line.inventoryItem.salePrice * line.quantity;
  const subtotal = roundMoney(Math.max(0, gross - line.discount));
  const tax = roundMoney(subtotal * line.inventoryItem.taxRate / 100);

  return {subtotal, tax, total: roundMoney(subtotal + tax)};
}

export function calculateTotals(lines: CommercialLineDraft[]): CommercialTotals {

  let subtotal = 0;
  let discount = 0;
  let tax = 0;

  for (const line of lines) {

    const result = calculateLine(line);

    subtotal += result.subtotal;
    discount += line.discount;
    tax += result.tax;
  }

  subtotal = roundMoney(subtotal);
  discount = roundMoney(discount);
  tax = roundMoney(tax);

  return {
    subtotal,
    discount,
    tax,
    total: roundMoney(subtotal + tax)
  };
}
