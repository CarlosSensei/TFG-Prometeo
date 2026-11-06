import { Component, DestroyRef, inject, signal } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Quote, QuoteSearch, QuoteStatus } from '../../models/quote.model';
import { QuoteApiService } from '../../services/quote-api.service';
import {
  TableActionEvent,
  TableFilterEvent,
  TableFilterValue,
  TableSort,
} from '../../../../shared/ui/data-table/data-table';
import {
  TableUrlConfig,
  readTableUrlState,
  writeTableUrlState,
} from '../../../../shared/utils/table-url-state';
import { ListNavigationStateService } from '../../../../core/services/list-navigation-state.service';
import { QuoteTable } from '../../components/quote-table/quote-table';
import { Button } from '../../../../shared/ui/button/button';
import { Badge } from '../../../../shared/ui/badge/badge';
import { QuoteDetailDrawer } from '../quote-detail-drawer/quote-detail-drawer';

@Component({
  selector: 'app-quote-list',
  standalone: true,
  imports: [QuoteTable, Button, Badge, QuoteDetailDrawer],
  templateUrl: './quote-list.html',
  styleUrl: './quote-list.scss',
})
export class QuoteList {
  private readonly api = inject(QuoteApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly listNavigation = inject(ListNavigationStateService);

  private request?: Subscription;

  readonly quotes = signal<Quote[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal(0);
  readonly pageSize = 10;
  readonly totalElements = signal(0);
  readonly selectedQuoteId = signal<number | null>(null);

  readonly sort = signal<TableSort>({
    key: 'quoteDate',
    direction: 'desc',
  });

  readonly filters = signal<Record<string, TableFilterValue>>({});

  private readonly urlConfig: TableUrlConfig = {
    allowedFilters: [
      'quoteNumber',
      'quoteDate',
      'expirationDate',
      'status',
      'total',
    ],

    allowedSorts: [
      'quoteNumber',
      'quoteDate',
      'expirationDate',
      'status',
      'total',
    ],

    defaultSort: {
      key: 'quoteDate',
      direction: 'desc',
    },
  };

  constructor() {
    // Cancelar peticiones HTTP al destruir
    // el componente.

    this.destroyRef.onDestroy(() => {
      this.request?.unsubscribe();
    });

    let previousTableState = '';

    // La URL es la fuente de verdad
    // de filtros, ordenación y paginación.

    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
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

        const listUrl = queryString ? `/quotes?${queryString}` : '/quotes';

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

  loadQuotes(): void {
    // Cancelar la consulta anterior si
    // se modifican rápidamente los filtros.

    this.request?.unsubscribe();

    this.loading.set(true);
    this.error.set(null);

    const f = this.filters();
    const sorting = this.sort();

    const selectedStatuses = (f['status']?.values ?? []) as QuoteStatus[];

    const search: QuoteSearch = {
      // Número de presupuesto
      quoteNumber: f['quoteNumber']?.text,

      // Multiselección de estados
      statuses: selectedStatuses.length ? selectedStatuses : undefined,

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
      sortDirection: sorting.direction,
    };

    this.request = this.api
      .search(search)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.quotes.set(response.content);
          this.totalElements.set(response.totalElements);
          this.page.set(response.number);
          this.loading.set(false);
        },

        error: (err) => {
          console.error('Error cargando presupuestos:', err);
          this.quotes.set([]);
          this.totalElements.set(0);
          this.loading.set(false);
          this.error.set('No se han podido cargar los presupuestos.');
        },
      });
  }

  private updateUrl(
    filters: Record<string, TableFilterValue>,
    sort: TableSort,
    page: number,
  ): void {
    const queryParams = writeTableUrlState(
      {
        filters,
        sort,
        page,
      },
      this.urlConfig,
    );

    // La suscripción del constructor
    // actualizará los signals, guardará
    // la vista y solicitará los datos.

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
    });
  }

  handleFilterChange(event: TableFilterEvent): void {
    const nextFilters = { ...this.filters(), [event.key]: event.value };

    // Volver a la primera página.

    this.updateUrl(nextFilters, this.sort(), 0);
  }

  clearAllFilters(): void {
    this.updateUrl({}, this.sort(), 0);
  }

  changeSort(sort: TableSort): void {
    this.updateUrl(this.filters(), sort, 0);
  }

  changePage(page: number): void {
    this.updateUrl(this.filters(), this.sort(), page);
  }

  retry(): void {
    this.loadQuotes();
  }

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

  closeQuoteDetail(): void {
    this.selectedQuoteId.set(null);
  }

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

    urlTree.queryParams = { ...urlTree.queryParams, detail: clientId };

    void this.router.navigateByUrl(urlTree);
  }

  createQuote(): void {
    // Utilizamos la URL guardada como retorno
    // para que QuoteForm pueda recuperarla.

    const returnUrl = this.listNavigation.get('quotes');

    void this.router.navigate(['/quotes/new'], {
      queryParams: {
        returnUrl,
      },
    });
  }

  editQuote(id: number): void {
    this.selectedQuoteId.set(null);

    const returnUrl = this.listNavigation.get('quotes');

    void this.router.navigate(['/quotes', id, 'edit'], {
      queryParams: {
        returnUrl,
      },
    });
  }
}
