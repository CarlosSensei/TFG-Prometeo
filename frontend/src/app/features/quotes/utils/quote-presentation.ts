import { QuoteStatus } from '../models/quote.model';
import { BadgeVariant } from '../../../shared/ui/badge/badge';

export const QUOTE_STATUS_OPTIONS: { value: QuoteStatus; label: string }[] = [
  { value: 'DRAFT', label: 'Borrador' },
  { value: 'SENT', label: 'Enviado' },
  { value: 'ACCEPTED', label: 'Aceptado' },
  { value: 'REJECTED', label: 'Rechazado' },
  { value: 'EXPIRED', label: 'Caducado' },
  { value: 'CONVERTED', label: 'Convertido' },
];

export function formatQuoteStatus(status: QuoteStatus): string {
  return (
    QUOTE_STATUS_OPTIONS.find((option) => option.value === status)?.label ??
    status
  );
}

export function quoteStatusVariant(status: QuoteStatus): BadgeVariant {
  const variants: Record<QuoteStatus, BadgeVariant> = {
    DRAFT: 'neutral',
    SENT: 'info',
    ACCEPTED: 'success',
    REJECTED: 'danger',
    EXPIRED: 'warning',
    CONVERTED: 'success',
  };
  return variants[status];
}

export function formatQuoteDate(value: string | null | undefined): string {
  if (!value) return '—';
  const [year, month, day] = value.split('-');
  return `${day}/${month}/${year}`;
}
