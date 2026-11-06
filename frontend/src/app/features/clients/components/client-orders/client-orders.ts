import { Component, effect, inject, input, signal } from '@angular/core';
import { DataTable, TableColumn, TableRow } from '../../../../shared/ui/data-table/data-table';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { ClientDocumentsApiService } from '../../services/client-documents-api.service';
import { ClientOrder } from '../../models/client-documents.model';

@Component({
  selector: 'app-client-orders',
  standalone: true,
  imports: [DataTable, Pagination],
  templateUrl: './client-orders.html',
  styleUrl: './client-orders.scss',
})
export class ClientOrders {
  private readonly documentsApi = inject(ClientDocumentsApiService);
  readonly clientId = input.required<number>();
  readonly documentPageSize = 5;
  readonly orderPage = signal(0);
  readonly orders = signal<ClientOrder[]>([]);
  readonly ordersTotal = signal(0);
  readonly documentsLoading = signal(false);
  readonly documentsError = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.clientId();
      this.orderPage.set(0);
    });
    effect((onCleanup) => {
      const id = this.clientId();
      const page = this.orderPage();
      this.orders.set([]);
      this.ordersTotal.set(0);
      this.documentsError.set(null);
      this.documentsLoading.set(true);
      const subscription = this.documentsApi
        .getOrders(id, page, this.documentPageSize)
        .subscribe({
          next: (response) => {
            this.orders.set(response.content);
            this.ordersTotal.set(response.totalElements);
            this.documentsLoading.set(false);
          },
          error: () => {
            this.documentsError.set('No se han podido recuperar los pedidos.');
            this.documentsLoading.set(false);
          },
        });
      onCleanup(() => subscription.unsubscribe());
    });
  }
  readonly orderColumns: TableColumn[] = [
    { key: 'orderNumber', label: 'Pedido' },
    { key: 'orderDate', label: 'Fecha' },
    { key: 'status', label: 'Estado' },
    { key: 'total', label: 'Importe' },
  ];

  get orderRows(): TableRow[] {
    return this.orders().map((order) => ({
      id: order.id,
      orderNumber: order.orderNumber,
      orderDate: order.orderDate,
      status: this.formatStatus(order.status),
      total: this.formatMoney(order.total),
    }));
  }
  formatMoney(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  }

  formatStatus(status: string | null): string {
    return status?.replaceAll('_', ' ') || 'Sin estado';
  }

  changeOrderPage(page: number): void {
    this.orderPage.set(page);
  }
}
