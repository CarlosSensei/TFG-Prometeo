import { OrderStatus } from '../models/order.model';
import { BadgeVariant } from '../../../shared/ui/badge/badge';

export const ORDER_STATUS_OPTIONS: {
  value: OrderStatus;
  label: string;
}[] = [
  { value: 'PENDING', label: 'Pendiente' },
  { value: 'CONFIRMED', label: 'Confirmado' },
  { value: 'IN_PROGRESS', label: 'En preparación' },
  { value: 'READY', label: 'Listo para entregar' },
  { value: 'DELIVERED', label: 'Entregado' },
  { value: 'CANCELLED', label: 'Cancelado' }
];

export function formatOrderStatus(status: OrderStatus): string {

  return ORDER_STATUS_OPTIONS.find(option => option.value === status)?.label ?? status;
}

export function orderStatusVariant(status: OrderStatus): BadgeVariant {

  switch (status) {
    case 'PENDING':
      return 'warning';

    case 'CONFIRMED':
    case 'IN_PROGRESS':
      return 'info';

    case 'READY':
    case 'DELIVERED':
      return 'success';

    case 'CANCELLED':
      return 'danger';
  }
}

export function formatOrderDate(value: string | null | undefined): string {

  if (!value) {
    return '—';
  }

  const [year, month, day] = value.split('T')[0].split('-');

  return `${day}/${month}/${year}`;
}
