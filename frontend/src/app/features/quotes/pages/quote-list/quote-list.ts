import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Quote, QuoteSearch, QuoteStatus } from '../../models/quote.model';
import { QuoteApiService } from '../../services/quote-api.service';
import {
  DataTable,
  TableColumn,
  TableRow,
  TableActionEvent,
  TableFilterEvent,
  TableFilterValue,
  TableSort
} from '../../../../shared/ui/data-table/data-table';
import { TableUrlConfig, readTableUrlState, writeTableUrlState } from '../../../../shared/utils/table-url-state';
import { ListNavigationStateService } from '../../../../core/services/list-navigation-state.service';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { Card } from '../../../../shared/ui/card/card';
import { Button } from '../../../../shared/ui/button/button';
import { Badge } from '../../../../shared/ui/badge/badge';
import { QuoteDetailDrawer } from '../quote-detail-drawer/quote-detail-drawer';

@Component({
  selector: 'app-quote-list',
  standalone: true,
  imports: [DataTable, Pagination, Card, Button, Badge, QuoteDetailDrawer],
  templateUrl: './quote-list.html',
  styleUrl: './quote-list.scss'
})
export class QuoteList {

  // ======================================
  // DEPENDENCIAS
  // ======================================

  private readonly api = inject(QuoteApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly listNavigation = inject(
    ListNavigationStateService
  );

  private request?: Subscription;

  // ======================================
  // ESTADO GENERAL
  // ======================================

  readonly quotes = signal<Quote[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal(0);
  readonly pageSize = 10;
  readonly totalElements = signal(0);
  readonly selectedQuoteId = signal<number | null>(null);

  // ======================================
  // ORDENACIÓN
  // ======================================

  readonly sort = signal<TableSort>({
    key: 'quoteDate',
    direction: 'desc'
  });

  // ======================================
  // FILTROS
  // ======================================

  readonly filters = signal<Record<string, TableFilterValue>>({});

  readonly statusOptions: { value: QuoteStatus; label: string; }[] = [
    { value: 'DRAFT', label: 'Borrador' },
    { value: 'SENT', label: 'Enviado' },
    { value: 'ACCEPTED', label: 'Aceptado' },
    { value: 'REJECTED', label: 'Rechazado' },
    { value: 'EXPIRED', label: 'Caducado' },
    { value: 'CONVERTED', label: 'Convertido' }
  ];

  readonly columns: TableColumn[] = [
    {
      key: 'quoteNumber',
      label: 'Presupuesto',
      sortable: true,
      filter: {
        type: 'text',
        placeholder: 'Número de presupuesto'
      }
    },
    {
      key: 'quoteDate',
      label: 'Fecha',
      sortable: true,
      filter: {
        type: 'date'
      }
    },
    {
      key: 'expirationDate',
      label: 'Caducidad',
      sortable: true,
      filter: {
        type: 'date'
      }
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
      filter: {
        type: 'number'
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
      'quoteNumber',
      'quoteDate',
      'expirationDate',
      'status',
      'total'
    ],

    allowedSorts: [
      'quoteNumber',
      'quoteDate',
      'expirationDate',
      'status',
      'total'
    ],

    defaultSort: {
      key: 'quoteDate',
      direction: 'desc'
    }
  };

  // ======================================
  // FILAS DE LA TABLA
  // ======================================

  readonly tableRows = computed<TableRow[]>(() =>
    this.quotes().map(quote => ({
      id: quote.id,
      quoteNumber: quote.quoteNumber,
      quoteDate: this.formatDate(quote.quoteDate),
      expirationDate: this.formatDate(quote.expirationDate),
      status: this.formatStatus(quote.status),
      total: this.formatMoney(quote.total)
    }))
  );

  // ======================================
  // INICIALIZACIÓN
  // ======================================

  constructor() {

    // Cancelar peticiones HTTP al destruir
    // el componente.

    this.destroyRef.onDestroy(() => {
      this.request?.unsubscribe();
    });

    let previousTableState = '';

    // La URL es la fuente de verdad
    // de filtros, ordenación y paginación.

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {

        // RECUPERAR ESTADO DE LA URL

        const state = readTableUrlState(params, this.urlConfig);

        // ==================================
        // PERSISTIR VISTA EN SESSIONSTORAGE
        // ==================================

        // Reconstruimos únicamente los parámetros
        // reconocidos por la tabla.
        //
        // Esto evita guardar parámetros ajenos,
        // como un futuro "detail" del drawer.

        const tableQuery = writeTableUrlState(state, this.urlConfig);
        const query = new URLSearchParams();

        for (const [key, value] of Object.entries(tableQuery)) {

          if (value !== null && value !== undefined) {
            query.set(key, String(value));
          }
        }

        const queryString = query.toString();

        const listUrl = queryString
          ? `/quotes?${queryString}` : '/quotes';

        // Guardar la última vista de Presupuestos.

        this.listNavigation.save('quotes', listUrl);

        // ==================================
        // SINCRONIZAR TABLA
        // ==================================

        const tableStateKey = JSON.stringify(state);

        // Evitamos solicitar nuevamente los
        // presupuestos si la URL cambia por
        // algún parámetro que no afecta
        // a la búsqueda.

        if (tableStateKey !== previousTableState) {

          previousTableState = tableStateKey;

          this.filters.set(state.filters);
          this.sort.set(state.sort);
          this.page.set(state.page);
          this.loadQuotes();
        }

      });
  }

  // ======================================
  // CARGA DE PRESUPUESTOS
  // ======================================

  loadQuotes(): void {

    // Cancelar la consulta anterior si
    // se modifican rápidamente los filtros.

    this.request?.unsubscribe();

    this.loading.set(true);
    this.error.set(null);

    const f = this.filters();
    const sorting = this.sort();

    const selectedStatuses = (
      f['status']?.values ?? []
    ) as QuoteStatus[];

    const search: QuoteSearch = {

      // Número de presupuesto
      quoteNumber: f['quoteNumber']?.text,

      // Multiselección de estados
      statuses: selectedStatuses.length
        ? selectedStatuses : undefined,

      // Fechas de creación del presupuesto
      quoteDateFrom: f['quoteDate']?.from,
      quoteDateTo: f['quoteDate']?.to,

      // Fechas de caducidad
      expirationDateFrom: f['expirationDate']?.from,
      expirationDateTo: f['expirationDate']?.to,

      // Rango económico
      minTotal: f['total']?.min ?? undefined,
      maxTotal: f['total']?.max ?? undefined,

      // Solo presupuestos activos
      active: true,

      // Paginación
      page: this.page(),
      size: this.pageSize,

      // Ordenación
      sortBy: sorting.key,
      sortDirection: sorting.direction
    };

    this.request = this.api
      .search(search)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({

        next: response => {

          this.quotes.set(response.content);
          this.totalElements.set(response.totalElements);
          this.page.set(response.number);
          this.loading.set(false);
        },

        error: err => {

          console.error('Error cargando presupuestos:', err);
          this.quotes.set([]);
          this.totalElements.set(0);
          this.loading.set(false);
          this.error.set('No se han podido cargar los presupuestos.');
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

    // La suscripción del constructor
    // actualizará los signals, guardará
    // la vista y solicitará los datos.

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams
    });
  }

  // ======================================
  // FILTROS
  // ======================================

  handleFilterChange(event: TableFilterEvent): void {

    const nextFilters = {...this.filters(), [event.key]: event.value};

    // Volver a la primera página.

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
    this.loadQuotes();
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

    this.selectedQuoteId.set(id);
  }

  // ======================================
  // DRAWER DE PRESUPUESTOS
  // ======================================

  closeQuoteDetail(): void {
    this.selectedQuoteId.set(null);
  }

  // ======================================
  // NAVEGACIÓN HACIA CLIENTES
  // ======================================

  openClientDetail(clientId: number): void {

    this.selectedQuoteId.set(null);

    // Recuperamos la última vista de Clientes.
    // De esta forma, no perdemos sus filtros
    // ni su posición de paginación.

    const clientsUrl = this.listNavigation.get('clients');

    // Convertimos la URL guardada en UrlTree.
    // Esto permite añadir el parámetro detail
    // conservando los filtros anteriores.

    const urlTree = this.router.parseUrl(clientsUrl);

    urlTree.queryParams = {...urlTree.queryParams, detail: clientId};

    void this.router.navigateByUrl(urlTree);
  }

  // ======================================
  // CREAR PRESUPUESTO
  // ======================================

  createQuote(): void {

    // Utilizamos la URL guardada como retorno
    // para que QuoteForm pueda recuperarla.

    const returnUrl = this.listNavigation.get('quotes');

    void this.router.navigate(['/quotes/new'],
      {
        queryParams: {
          returnUrl
        }
      }
    );
  }

  // ======================================
  // EDITAR PRESUPUESTO
  // ======================================

  editQuote(id: number): void {

    this.selectedQuoteId.set(null);

    const returnUrl = this.listNavigation.get('quotes');

    void this.router.navigate(['/quotes', id, 'edit'],
      {
        queryParams: {
          returnUrl
        }
      }
    );
  }

  // ======================================
  // FORMATOS
  // ======================================

  formatStatus(status: QuoteStatus): string {

    return this.statusOptions.find(
      item => item.value === status
    )?.label ?? status;
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
