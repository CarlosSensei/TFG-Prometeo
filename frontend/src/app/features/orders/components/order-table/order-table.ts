
import { Component, computed, input, output } from '@angular/core';
import { Order, OrderStatus } from '../../models/order.model';
import {
  DataTable,
  TableColumn,
  TableRow,
  TableFilterValue,
  TableFilterEvent,
  TableSort,
  TableActionEvent
} from '../../../../shared/ui/data-table/data-table';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { Card } from '../../../../shared/ui/card/card';
import { Button } from '../../../../shared/ui/button/button';

@Component({
  selector: 'app-order-table',
  standalone: true,
  imports: [ DataTable, Pagination, Card, Button],
  templateUrl: './order-table.html',
  styleUrl: './order-table.scss'
})
export class OrderTable {

  readonly orders = input.required<Order[]>();
  readonly loading = input(false);
  readonly filters = input.required<Record<string, TableFilterValue>>();
  readonly sort = input.required<TableSort>();
  readonly page = input(0);
  readonly pageSize = input(10);
  readonly totalElements = input(0);
  readonly filtersCleared = output<void>();
  readonly filterChanged = output<TableFilterEvent>();
  readonly sortChanged = output<TableSort>();
  readonly rowAction = output<TableActionEvent>();
  readonly pageChanged = output<number>();

  readonly statusOptions: { value: OrderStatus; label: string; }[] = [
    { value: 'PENDING', label: 'Pendiente' },
    { value: 'CONFIRMED', label: 'Confirmado' },
    { value: 'IN_PROGRESS', label: 'En preparación' },
    { value: 'READY', label: 'Listo' },
    { value: 'DELIVERED', label: 'Entregado' },
    { value: 'CANCELLED', label: 'Cancelado' }
  ];

  readonly columns: TableColumn[] = [
    {
      key: 'orderNumber',
      label: 'Pedido',
      sortable: true,
      filter: {
        type: 'text',
        placeholder: 'Número de pedido'
      }
    },
    {
      key: 'orderDate',
      label: 'Fecha',
      sortable: true,
      filter: { type: 'date' }
    },
    {
      key: 'expectedDeliveryDate',
      label: 'Entrega prevista',
      sortable: true,
      filter: { type: 'date' }
    },
    {
      key: 'status',
      label: 'Estado',
      sortable: true,
      filter: {
        type: 'multi',
        options: this.statusOptions
      }
    },
    {
      key: 'total',
      label: 'Importe',
      sortable: true,
      filter: { type: 'number' }
    },
    {
      key: 'actions',
      label: 'Acciones',
      sortable: false
    }
  ];

  readonly tableRows = computed<TableRow[]>(() =>
    this.orders().map(order => ({
      id: order.id,
      orderNumber: order.orderNumber,
      orderDate: this.formatDate(order.orderDate),
      expectedDeliveryDate: this.formatDate(order.expectedDeliveryDate),
      status: this.formatStatus(order.status),
      total: this.formatMoney(order.total)
    }))
  );

  formatStatus(status: OrderStatus): string {

    return this.statusOptions.find(option => option.value === status)?.label ?? status;
  }

  formatDate(value: string | null): string {

    if (!value) {
      return '—';
    }

    const [year, month, day] = value.split('-');

    return `${day}/${month}/${year}`;
  }

  formatMoney(value: number): string {

    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(value);
  }
}
