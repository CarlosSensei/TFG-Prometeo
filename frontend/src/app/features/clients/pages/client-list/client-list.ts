import { Component, DestroyRef, inject, signal } from '@angular/core';
import { Subscription } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, ActivatedRoute } from '@angular/router';
import { ClientApiService, ClientTableSearch } from '../../services/client-api.service';
import { Client } from '../../models/client.model';
import {
  DataTable,
  TableColumn,
  TableRow,
  TableActionEvent,
  TableFilterEvent,
  TableFilterValue,
  TableSort
} from '../../../../shared/ui/data-table/data-table';
import {
  TableUrlConfig,
  readTableUrlState,
  writeTableUrlState } from '../../../../shared/utils/table-url-state';
import { ListNavigationStateService } from '../../../../core/services/list-navigation-state.service';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { Card } from '../../../../shared/ui/card/card';
import { Badge } from '../../../../shared/ui/badge/badge';
import { Button } from '../../../../shared/ui/button/button';
import { ClientDetailDrawer } from '../../components/client-detail-drawer/client-detail-drawer';

@Component({
  selector: 'app-client-list',
  standalone: true,
  imports: [DataTable, Pagination, Card, Badge, Button, ClientDetailDrawer],
  templateUrl: './client-list.html',
  styleUrl: './client-list.scss'
})
export class ClientList {

  // ======================================
  // DEPENDENCIAS
  // ======================================

  private readonly clientApi = inject(ClientApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly listNavigation = inject(ListNavigationStateService);
  private request?: Subscription;

  // ======================================
  // ESTADO GENERAL
  // ======================================

  readonly clients = signal<Client[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal(0);
  readonly size = 10;
  readonly totalElements = signal(0);
  readonly selectedClientId = signal<number | null>(null);
  readonly detailClientId = signal<number | null>(null);

  // ======================================
  // ORDENACIÓN
  // ======================================

  readonly sort = signal<TableSort>({
    key: 'name',
    direction: 'asc'
  });

  // ======================================
  // FILTROS
  // ======================================

  readonly filters = signal<
    Record<string, TableFilterValue>
  >({});

  readonly columns: TableColumn[] = [
    {
      key: 'name',
      label: 'Cliente',
      sortable: true,
      filter: {
        type: 'text',
        placeholder: 'Nombre o apellidos'
      }
    },
    {
      key: 'dni',
      label: 'DNI / NIE',
      sortable: true,
      filter: {
        type: 'text'
      }
    },
    {
      key: 'email',
      label: 'Email',
      sortable: true,
      filter: {
        type: 'text'
      }
    },
    {
      key: 'phoneNumber',
      label: 'Teléfono',
      sortable: true,
      filter: {
        type: 'text'
      }
    },
    {
      key: 'city',
      label: 'Ciudad',
      sortable: true,
      filter: {
        type: 'text'
      }
    },
    {
      key: 'actions',
      label: 'Acciones',
      sortable: false
    }
  ];

  // ======================================
  // CONFIGURACIÓN DE URL
  // ======================================

  private readonly urlConfig: TableUrlConfig = {

    allowedFilters: [
      'name',
      'dni',
      'email',
      'phoneNumber',
      'city'
    ],

    allowedSorts: [
      'name',
      'dni',
      'email',
      'phoneNumber',
      'city'
    ],

    defaultSort: {
      key: 'name',
      direction: 'asc'
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

        // Recuperar filtros, ordenación y página
        // desde los parámetros de la URL.

        const state = readTableUrlState(params, this.urlConfig);

        // ==================================
        // PERSISTENCIA DE LA VISTA
        // ==================================

        // Reconstruimos únicamente los parámetros
        // correspondientes a la tabla.
        //
        // No guardamos "detail", porque no queremos
        // reabrir automáticamente un drawer al
        // regresar desde otro módulo.

        const tableQuery = writeTableUrlState(state, this.urlConfig);

        const query = new URLSearchParams();

        for (const [key, value] of Object.entries(tableQuery)) {

          if (value !== null && value !== undefined) {
            query.set(key, String(value));
          }
        }

        const queryString = query.toString();
        const listUrl = queryString
          ? `/clients?${queryString}` : '/clients';

        // Guardamos la última vista de Clientes
        // en sessionStorage mediante el servicio.

        this.listNavigation.save('clients', listUrl);

        // ==================================
        // SINCRONIZACIÓN DE LA TABLA
        // ==================================

        const tableStateKey = JSON.stringify(state);

        // Solo consultamos el backend cuando
        // cambian filtros, ordenación o página.
        //
        // Abrir o cerrar el drawer no debe
        // provocar una consulta del listado.

        if (tableStateKey !== previousTableState) {

          previousTableState = tableStateKey;

          this.filters.set(state.filters);
          this.sort.set(state.sort);
          this.page.set(state.page);
          this.selectedClientId.set(null);
          this.loadClients();
        }

        // ==================================
        // DRAWER DEL CLIENTE
        // ==================================

        const detailParam = params.get('detail');

        const detail = detailParam !== null
          ? Number(detailParam) : null;

        this.detailClientId.set(
          detail !== null &&
          Number.isSafeInteger(detail) &&
          detail > 0 ? detail : null
        );

      });
  }

  // ======================================
  // FILAS DE LA TABLA
  // ======================================

  get tableRows(): TableRow[] {

    return this.clients().map(client => ({

      id: client.id,
      name: [
        client.name,
        client.surname,
        client.lastSurname
      ].filter(Boolean).join(' '),
      dni: client.dni,
      email: client.email ?? '—',
      phoneNumber: client.phoneNumber ?? '—',
      city: client.city ?? '—'

    }));
  }

  // ======================================
  // CARGA DE CLIENTES
  // ======================================

  loadClients(): void {

    // Cancelamos la consulta anterior
    // si se han cambiado filtros rápidamente.

    this.request?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);

    const filters = this.filters();
    const sorting = this.sort();

    // La columna visual "Cliente" contiene
    // el nombre completo, pero ordenamos
    // por el atributo surname de la entidad.

    const sortBy = sorting.key === 'name'
      ? 'surname' : sorting.key;

    const search: ClientTableSearch = {

      fullName: filters['name']?.text,
      dni: filters['dni']?.text,
      email: filters['email']?.text,
      phoneNumber: filters['phoneNumber']?.text,
      city: filters['city']?.text,
      active: true,
      page: this.page(),
      size: this.size,
      sortBy,
      sortDirection: sorting.direction
    };

    this.request = this.clientApi
      .search(search)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({

        next: response => {

          this.clients.set(response.content);
          this.totalElements.set(response.totalElements);
          this.page.set(response.number);
          this.loading.set(false);
        },

        error: err => {

          console.error('Error cargando clientes:', err);

          this.clients.set([]);
          this.totalElements.set(0);
          this.loading.set(false);
          this.error.set('No se pudieron cargar los clientes.');
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
      {
        filters,
        sort,
        page
      },
      this.urlConfig
    );

    // Si existe un drawer abierto, conservamos
    // su identificador al modificar la tabla.

    const detail = this.route.snapshot.queryParamMap.get('detail');

    if (detail) {
      queryParams['detail'] = detail;
    }

    // La suscripción a queryParamMap se
    // encargará de actualizar los signals,
    // consultar el backend y guardar la vista.

    void this.router.navigate([], {relativeTo: this.route, queryParams});
  }

  // ======================================
  // FILTROS
  // ======================================

  handleFilterChange(event: TableFilterEvent): void {

    const nextFilters = {...this.filters(), [event.key]: event.value};

    // Cada cambio de filtro vuelve a
    // la primera página.

    this.updateUrl(nextFilters, this.sort(), 0);
  }

  clearAllFilters(): void {

    this.updateUrl({}, this.sort(), 0);
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
    this.loadClients();
  }

  // ======================================
  // ACCIONES DE LA TABLA
  // ======================================

  handleRowAction(event: TableActionEvent): void {

    if (event.action !== 'view') {
      return;
    }

    const id = Number(event.row.id);

    if (!Number.isSafeInteger(id) || id <= 0) {
      return;
    }

    this.openClientDetail(id);
  }

  selectClient(row: TableRow): void {
    this.selectedClientId.set(Number(row.id));
  }

  // ======================================
  // NAVEGACIÓN
  // ======================================

  createClient(): void {

    void this.router.navigate(['/clients/new'],
      {
        queryParams: {
          returnUrl: this.listNavigation.get('clients')
        }
      }
    );
  }

  openClientDetail(id: number): void {

    void this.router.navigate([], {
      relativeTo: this.route,

      queryParams: {
        detail: id
      },

      queryParamsHandling: 'merge'
    });
  }

  closeClientDetail(): void {

    void this.router.navigate([], {
      relativeTo: this.route,

      queryParams: {
        detail: null
      },

      queryParamsHandling: 'merge',
      replaceUrl: true
    });
  }

  editSelectedClient(): void {

    const id = this.selectedClientId();

    if (id === null) {
      return;
    }

    void this.router.navigate(['/clients', id, 'edit'], 
      {
        queryParams: {
          returnUrl: this.listNavigation.get('clients')
        }
      });
  }
}
