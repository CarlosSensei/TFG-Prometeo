import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { Card } from '../../../../shared/ui/card/card';
import { Button } from '../../../../shared/ui/button/button';
import { DataTable, TableColumn, TableRow, TableSort, TableFilterValue, TableFilterEvent, TableActionEvent } from '../../../../shared/ui/data-table/data-table';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { TableUrlConfig, readTableUrlState, writeTableUrlState } from '../../../../shared/utils/table-url-state';
import { ListNavigationStateService } from '../../../../core/services/list-navigation-state.service';
import { FollowUp, FollowUpSearch, FollowUpStatus, FollowUpType, FOLLOWUP_STATUSES, FOLLOWUP_TYPES } from '../../models/followup.model';
import { FollowUpApiService } from '../../services/followup-api.service';
import { FollowUpDetailDrawer } from '../followup-detail-drawer/followup-detail-drawer';

@Component({
  selector: 'app-followup-list',
  standalone: true,
  imports: [Card, Button, DataTable, Pagination, FollowUpDetailDrawer],
  templateUrl: './followup-list.html',
  styleUrl: './followup-list.scss'
})
export class FollowUpList {
  private readonly api = inject(FollowUpApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly navigation = inject(ListNavigationStateService);
  private request?: Subscription;

  readonly types = FOLLOWUP_TYPES;
  readonly statuses = FOLLOWUP_STATUSES;
  readonly items = signal<FollowUp[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly totalElements = signal(0);
  readonly page = signal(0);
  readonly pageSize = 10;
  readonly selectedFollowUp = signal<FollowUp | null>(null);
  readonly filters = signal<Record<string, TableFilterValue>>({});
  readonly sort = signal<TableSort>({ key: 'scheduledAt', direction: 'asc' });

  private readonly urlConfig: TableUrlConfig = {
    allowedFilters: ['quoteId', 'type', 'scheduledAt', 'status', 'origin'],
    allowedSorts: ['scheduledAt', 'createdAt', 'completedAt', 'type', 'status'],
    defaultSort: { key: 'scheduledAt', direction: 'asc' }
  };

  readonly columns: TableColumn[] = [
    { key: 'quoteId', label: 'Presupuesto', filter: { type: 'text', placeholder: 'ID de presupuesto' } },
    { key: 'type', label: 'Tipo', sortable: true, filter: { type: 'multi', options: FOLLOWUP_TYPES } },
    { key: 'scheduledAt', label: 'Programado para', sortable: true, filter: { type: 'date' } },
    { key: 'status', label: 'Estado', sortable: true, filter: { type: 'multi', options: FOLLOWUP_STATUSES } },
    { key: 'origin', label: 'Origen', filter: { type: 'multi', options: [{ value: 'MANUAL', label: 'Manual' }, { value: 'AUTOMATED', label: 'Automático' }] } },
    { key: 'actions', label: 'Acciones' }
  ];

  readonly rows = computed<TableRow[]>(() => this.items().map(item => ({
    id: item.id,
    quoteId: `#${item.quoteId}`,
    type: this.types.find(t => t.value === item.type)?.label ?? item.type,
    scheduledAt: this.formatDateTime(item.scheduledAt),
    status: this.statuses.find(s => s.value === item.status)?.label ?? item.status,
    origin: item.automated ? 'Automático' : 'Manual'
  })));

  constructor() {
    this.destroyRef.onDestroy(() => this.request?.unsubscribe());
    let previousState = '';
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      const state = readTableUrlState(params, this.urlConfig);
      const query = new URLSearchParams();
      for (const [key, value] of Object.entries(writeTableUrlState(state, this.urlConfig))) {
        if (value != null) query.set(key, String(value));
      }
      this.navigation.save('followups', query.size ? `/followups?${query}` : '/followups');
      const stateKey = JSON.stringify(state);
      if (stateKey === previousState) return;
      previousState = stateKey;
      this.filters.set(state.filters);
      this.sort.set(state.sort);
      this.page.set(state.page);
      this.selectedFollowUp.set(null);
      this.load();
    });
  }

  private formatDateTime(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '—' :
      new Intl.DateTimeFormat('es-ES', { dateStyle: 'short', timeStyle: 'short' }).format(date);
  }

  load(): void {
    this.request?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);
    const f = this.filters();
    const quote = f['quoteId']?.text?.trim();
    const quoteId = quote ? Number(quote.replace(/^#/, '')) : undefined;
    if (quoteId !== undefined && (!Number.isSafeInteger(quoteId) || quoteId <= 0)) {
      this.loading.set(false);
      this.items.set([]);
      this.totalElements.set(0);
      this.error.set('El ID del presupuesto debe ser un entero positivo.');
      return;
    }
    const statusValues = f['status']?.values ?? [];
    const typeValues = f['type']?.values ?? [];
    const originValues = f['origin']?.values ?? [];
    // Multiselección: si se marcan ambos orígenes, no filtramos por origen.
    const automated = originValues.length === 1
      ? originValues[0] === 'AUTOMATED'
      : undefined;
    const start = f['scheduledAt']?.from;
    const end = f['scheduledAt']?.to;
    if (start && end && start > end) {
      this.loading.set(false);
      this.items.set([]);
      this.totalElements.set(0);
      this.error.set('La fecha inicial no puede superar la final.');
      return;
    }
    const criteria: FollowUpSearch = {
      active: true,
      page: this.page(),
      size: this.pageSize,
      sortBy: this.sort().key as FollowUpSearch['sortBy'],
      sortDirection: this.sort().direction,
      ...(quoteId !== undefined ? { quoteId } : {}),
      ...(statusValues.length ? { statuses: statusValues as FollowUpStatus[] } : {}),
      ...(typeValues.length ? { types: typeValues as FollowUpType[] } : {}),
      ...(automated !== undefined ? { automated } : {}),
      ...(start ? { scheduledFrom: `${start}T00:00:00` } : {}),
      ...(end ? { scheduledTo: `${end}T23:59:59` } : {})
    };
    this.request = this.api.search(criteria).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: response => {
        this.items.set(response.content);
        this.totalElements.set(response.totalElements);
        this.page.set(response.number);
        this.loading.set(false);
      },
      error: err => {
        console.error('Error consultando seguimientos:', err);
        this.items.set([]);
        this.totalElements.set(0);
        this.error.set('No se han podido cargar los seguimientos.');
        this.loading.set(false);
      }
    });
  }

  private updateUrl(filters: Record<string, TableFilterValue>, sort: TableSort, page: number): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: writeTableUrlState({ filters, sort, page }, this.urlConfig)
    });
  }

  handleFilterChange(event: TableFilterEvent): void {
    // DataTable conserva todos los valores marcados en la URL.
    this.updateUrl({ ...this.filters(), [event.key]: event.value }, this.sort(), 0);
  }

  clearFilters(): void { this.updateUrl({}, this.sort(), 0); }
  changePage(page: number): void { this.updateUrl(this.filters(), this.sort(), page); }
  changeSort(sort: TableSort): void {
    if (!this.urlConfig.allowedSorts.includes(sort.key)) return;
    this.updateUrl(this.filters(), sort, 0);
  }

  handleRowAction(event: TableActionEvent): void {
    if (event.action !== 'view') return;
    const id = Number(event.row.id);
    if (!Number.isSafeInteger(id) || id <= 0) return;
    const item = this.items().find(followUp => followUp.id === id);
    if (!item) return;
    this.selectedFollowUp.set(item);
  }

  closeDetail(): void { this.selectedFollowUp.set(null); }

  onFollowUpUpdated(): void {
    this.closeDetail();
    this.load();
  }
}
