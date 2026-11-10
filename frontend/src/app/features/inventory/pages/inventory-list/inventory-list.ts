import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Card } from '../../../../shared/ui/card/card';
import { Button } from '../../../../shared/ui/button/button';
import { Badge } from '../../../../shared/ui/badge/badge';
import { DataTable, TableActionEvent, TableColumn, TableFilterEvent, TableFilterValue, TableRow, TableSort } from '../../../../shared/ui/data-table/data-table';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { readTableUrlState, TableUrlConfig, writeTableUrlState } from '../../../../shared/utils/table-url-state';
import { ListNavigationStateService } from '../../../../core/services/list-navigation-state.service';
import { INVENTORY_TYPES, InventoryItem, InventorySearch, InventoryType } from '../../models/inventory.model';
import { InventoryApiService } from '../../services/inventory-api.service';
import { InventoryDetailDrawer } from '../../components/inventory-detail-drawer/inventory-detail-drawer';

@Component({
  selector: 'app-inventory-list',
  standalone: true,
  imports: [Card, Button, Badge, DataTable, Pagination, InventoryDetailDrawer],
  templateUrl: './inventory-list.html',
  styleUrl: './inventory-list.scss',
})
export class InventoryList {
  private readonly api = inject(InventoryApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly navigation = inject(ListNavigationStateService);
  private request?: Subscription;

  readonly items = signal<InventoryItem[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal(0);
  readonly size = 10;
  readonly totalElements = signal(0);
  readonly filters = signal<Record<string, TableFilterValue>>({});
  readonly sort = signal<TableSort>({key: 'name', direction: 'asc'});
  readonly selectedId = signal<number | null>(null);
  readonly creating = signal(false);

  readonly columns: TableColumn[] = [
    {key: 'sku', label: 'SKU', sortable: true, filter: {type: 'text'}},
    {key: 'name', label: 'Artículo', sortable: true, filter: {type: 'text'}},
    {key: 'brand', label: 'Marca', sortable: true, filter: {type: 'text'}},
    {key: 'type', label: 'Familia', sortable: true, filter: {type: 'multi', options: INVENTORY_TYPES}},
    {key: 'salePrice', label: 'Precio', sortable: true, filter: {type: 'number'}},
    {key: 'stock', label: 'Stock', sortable: true},
    {key: 'actions', label: 'Acciones'},
  ];

  readonly rows = computed<TableRow[]>(() => this.items().map(item => ({
    id: item.id,
    sku: item.sku,
    name: item.name,
    brand: item.brand ?? '—',
    type: INVENTORY_TYPES.find(type => type.value === item.type)?.label ?? item.type,
    salePrice: new Intl.NumberFormat('es-ES', {style: 'currency', currency: 'EUR'}).format(item.salePrice),
    stock: item.type === 'SERVICE' ? 'No aplica' : item.stock,
  })));

  private readonly config: TableUrlConfig = {
    allowedFilters: ['sku', 'name', 'brand', 'type', 'salePrice'],
    allowedSorts: ['sku', 'name', 'brand', 'type', 'salePrice', 'stock'],
    defaultSort: {key: 'name', direction: 'asc'},
  };

  constructor() {
    this.destroyRef.onDestroy(() => this.request?.unsubscribe());
    let previous = '';
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      const state = readTableUrlState(params, this.config);
      const urlParams = new URLSearchParams();
      for (const [key, value] of Object.entries(writeTableUrlState(state, this.config))) {
        if (value != null) urlParams.set(key, String(value));
      }
      const suffix = urlParams.toString();
      this.navigation.save('inventory', suffix ? `/inventory?${suffix}` : '/inventory');
      const signature = JSON.stringify(state);
      if (signature === previous) return;
      previous = signature;
      this.filters.set(state.filters);
      this.sort.set(state.sort);
      this.page.set(state.page);
      this.reload();
    });
  }

  reload(): void {
    this.request?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);
    const f = this.filters();
    const selectedTypes = (f['type']?.values ?? []).filter(value =>
      INVENTORY_TYPES.some(type => type.value === value)
    ) as InventoryType[];
    const payload: InventorySearch = {
      sku: f['sku']?.text || undefined,
      name: f['name']?.text || undefined,
      brand: f['brand']?.text || undefined,
      types: selectedTypes.length ? selectedTypes : undefined,
      minPrice: f['salePrice']?.min ?? undefined,
      maxPrice: f['salePrice']?.max ?? undefined,
      active: true,
      page: this.page(), size: this.size,
      sortBy: this.sort().key,
      sortDirection: this.sort().direction,
    };
    this.request = this.api.search(payload).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: page => {
        this.items.set(page.content);
        this.totalElements.set(page.totalElements);
        this.page.set(page.number);
        this.loading.set(false);
      },
      error: err => {
        console.error('No se pudo cargar Inventario', err);
        this.items.set([]);
        this.totalElements.set(0);
        this.loading.set(false);
        this.error.set(err.error?.message || 'No se ha podido cargar el inventario.');
      },
    });
  }

  private updateUrl(filters: Record<string, TableFilterValue>, sort: TableSort, page: number): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: writeTableUrlState({filters, sort, page}, this.config),
    });
  }

  onFilter(event: TableFilterEvent): void {
    this.updateUrl({...this.filters(), [event.key]: event.value}, this.sort(), 0);
  }
  onSort(sort: TableSort): void { this.updateUrl(this.filters(), sort, 0); }
  onPage(page: number): void { this.updateUrl(this.filters(), this.sort(), page); }
  clearFilters(): void { this.updateUrl({}, this.sort(), 0); }

  onAction(event: TableActionEvent): void { this.openRow(event.row); }
  openRow(row: TableRow): void {
    const id = Number(row.id);
    if (Number.isSafeInteger(id) && id > 0) {
      this.creating.set(false);
      this.selectedId.set(id);
    }
  }
  newItem(): void { this.selectedId.set(null); this.creating.set(true); }
  closeDrawer(): void { this.selectedId.set(null); this.creating.set(false); }
  onSaved(): void { this.closeDrawer(); this.reload(); }
  onStockUpdated(): void { this.reload(); }
}
