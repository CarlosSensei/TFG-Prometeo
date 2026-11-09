
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Order, OrderSearch, OrderStatus } from '../../models/order.model';
import { OrderApiService } from '../../services/order-api.service';
import {
  TableActionEvent,
  TableFilterEvent,
  TableFilterValue,
  TableSort
} from '../../../../shared/ui/data-table/data-table';
import { TableUrlConfig, readTableUrlState, writeTableUrlState } from '../../../../shared/utils/table-url-state';
import { ListNavigationStateService } from '../../../../core/services/list-navigation-state.service';
import { OrderTable } from '../../components/order-table/order-table';
import { OrderDetailDrawer } from '../order-detail-drawer/order-detail-drawer';
import { Button } from '../../../../shared/ui/button/button';
import { Badge } from '../../../../shared/ui/badge/badge';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [OrderTable, Button, Badge, OrderDetailDrawer],
  templateUrl: './order-list.html',
  styleUrl: './order-list.scss'
})
export class OrderList {

  // ======================================
  // DEPENDENCIAS
  // ======================================

  private readonly api = inject(OrderApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly listNavigation = inject(ListNavigationStateService);
  private request?: Subscription;

  // ======================================
  // ESTADO GENERAL
  // ======================================

  readonly orders = signal<Order[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal(0);
  readonly pageSize = 10;
  readonly totalElements = signal(0);
  readonly selectedOrderId = signal<number | null>(null);

  // ======================================
  // FILTROS Y ORDENACIÓN
  // ======================================

  readonly filters = signal<Record<string, TableFilterValue>>({});
  readonly sort = signal<TableSort>({key: 'orderDate', direction: 'desc'});

  // ======================================
  // CONFIGURACIÓN DE URL
  // ======================================

  private readonly urlConfig: TableUrlConfig = {

    allowedFilters: [
      'orderNumber',
      'orderDate',
      'expectedDeliveryDate',
      'status',
      'total'
    ],

    allowedSorts: [
      'orderNumber',
      'orderDate',
      'expectedDeliveryDate',
      'status',
      'total'
    ],

    defaultSort: {
      key: 'orderDate',
      direction: 'desc'
    }
  };

  // ======================================
  // INICIALIZACIÓN
  // ======================================

  constructor() {

    this.destroyRef.onDestroy(() => {
      this.request?.unsubscribe();
    });

    let previousTableState = '';

    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {

        // Recuperar la configuración de la tabla.
        const state = readTableUrlState(params, this.urlConfig);

        // Guardar únicamente filtros,
        // ordenación y paginación.
        const tableQuery = writeTableUrlState(state, this.urlConfig);

        const query = new URLSearchParams();

        for (const [key, value] of Object.entries(tableQuery)) {
          if (value !== null && value !== undefined) {
            query.set(key, String(value));
          }
        }

        const queryString = query.toString();

        this.listNavigation.save('orders', queryString 
            ? `/orders?${queryString}` : '/orders');

        const tableStateKey = JSON.stringify(state);

        // Evitar peticiones innecesarias.
        if (tableStateKey !== previousTableState) {

          previousTableState = tableStateKey;

          this.filters.set(state.filters);
          this.sort.set(state.sort);
          this.page.set(state.page);
          this.selectedOrderId.set(null);
          this.loadOrders();
        }
      });
  }

  // ======================================
  // CONSULTA AL BACKEND
  // ======================================

  loadOrders(): void {

    this.request?.unsubscribe();

    this.loading.set(true);
    this.error.set(null);

    const f = this.filters();
    const sorting = this.sort();

    const selectedStatuses = (
      f['status']?.values ?? []
    ) as OrderStatus[];

    const search: OrderSearch = {

      orderNumber: f['orderNumber']?.text,

      statuses: selectedStatuses.length ? selectedStatuses : undefined,

      orderDateFrom: f['orderDate']?.from,
      orderDateTo: f['orderDate']?.to,

      expectedDeliveryDateFrom: f['expectedDeliveryDate']?.from,
      expectedDeliveryDateTo: f['expectedDeliveryDate']?.to,

      minTotal: f['total']?.min ?? undefined,
      maxTotal: f['total']?.max ?? undefined,

      active: true,

      page: this.page(),
      size: this.pageSize,

      sortBy: sorting.key,
      sortDirection: sorting.direction
    };

    this.request = this.api
      .search(search)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({

        next: response => {

          this.orders.set(response.content);
          this.totalElements.set(response.totalElements);
          this.page.set(response.number);
          this.loading.set(false);
        },

        error: err => {

          console.error('Error cargando pedidos:', err);

          this.orders.set([]);
          this.totalElements.set(0);
          this.error.set('No se han podido cargar los pedidos.');
          this.loading.set(false);
        }
      });
  }

  // ======================================
  // CREATE
  // ======================================

  createOrder(): void {

    const returnUrl = this.listNavigation.get('orders');

    void this.router.navigate(['/orders/new'], {
      queryParams: {
        returnUrl
      }
    });
  }

  // ======================================
  // ACTUALIZAR URL
  // ======================================

  private updateUrl(
    filters: Record<string, TableFilterValue>,
    sort: TableSort,
    page: number
  ): void {

    const queryParams = writeTableUrlState(
      { filters, sort, page },
      this.urlConfig
    );

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams
    });
  }

  // ======================================
  // FILTROS
  // ======================================

  handleFilterChange(event: TableFilterEvent): void {

    this.updateUrl(
      {
        ...this.filters(),
        [event.key]: event.value
      },
      this.sort(),
      0
    );
  }

  clearAllFilters(): void {

    this.updateUrl(
      {},
      this.sort(),
      0
    );
  }

  // ======================================
  // ORDENACIÓN
  // ======================================

  changeSort(sort: TableSort): void {

    this.updateUrl(this.filters(), sort, 0);
  }

  // ======================================
  // PAGINACIÓN
  // ======================================

  changePage(page: number): void {

    this.updateUrl(this.filters(), this.sort(), page);
  }

  // ======================================
  // REINTENTAR
  // ======================================

  retry(): void {
    this.loadOrders();
  }

  // ======================================
  // ACCIONES
  // ======================================

  handleRowAction(event: TableActionEvent): void {

    if (event.action !== 'view') {
      return;
    }

    const id = Number(event.row.id);

    if (
      !Number.isSafeInteger(id) ||
      id <= 0
    ) {
      return;
    }

    // Reservamos el ID para OrderDetailDrawer,
    // que implementaremos en el siguiente paso.
    this.selectedOrderId.set(id);
  }

  // ======================================
  // DETAILS
  // ======================================

  closeOrderDetail(): void {
    this.selectedOrderId.set(null);
  }

  openClientDetail(clientId: number): void {

    this.selectedOrderId.set(null);

    // Recuperamos los filtros y la paginación
    // guardados del listado de clientes.
    const clientsUrl = this.listNavigation.get('clients');
    const urlTree = this.router.parseUrl(clientsUrl);

    // Abrimos directamente la ficha del cliente.
    urlTree.queryParams = {
      ...urlTree.queryParams,
      detail: clientId
    };

    void this.router.navigateByUrl(urlTree);
  }

  onOrderUpdated(): void {

    // Recargar conservando la URL actual,
    // filtros, página y ordenación.
    this.loadOrders();
  }


}
