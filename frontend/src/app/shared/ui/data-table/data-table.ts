import {
  Component,
  ElementRef,
  HostListener,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import {
  TableActionEvent,
  TableColumn,
  TableFilterEvent,
  TableFilterValue,
  TableRow,
  TableSort,
} from './data-table.models';
import { TableHeader } from './table-header/table-header';
import { TableBody } from './table-body/table-body';
import { TableActiveFilters } from './table-active-filters/table-active-filters';
import { TableFilterPanel } from './table-filter-panel/table-filter-panel';

export type {
  FilterType,
  FilterOption,
  TableFilterValue,
  TableColumn,
  TableRow,
  TableActionEvent,
  TableFilterEvent,
  SortDirection,
  TableSort,
} from './data-table.models';

@Component({
  selector: 'app-data-table',
  imports: [TableHeader, TableBody, TableActiveFilters, TableFilterPanel],
  templateUrl: './data-table.html',
  styleUrl: './data-table.scss',
})
export class DataTable {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly columns = input.required<TableColumn[]>();
  readonly rows = input.required<TableRow[]>();
  readonly loading = input(false);
  readonly emptyMessage = input('No se encontraron registros');
  readonly filters = input<Record<string, TableFilterValue>>({});
  readonly sort = input<TableSort | null>(null);

  readonly rowSelected = output<TableRow>();
  readonly rowAction = output<TableActionEvent>();
  readonly filterChange = output<TableFilterEvent>();
  readonly sortChange = output<TableSort>();

  readonly openFilter = signal<string | null>(null);
  readonly panelPosition = signal({ top: 0, left: 0 });
  readonly currentColumn = computed(() =>
    this.columns().find((column) => column.key === this.openFilter()),
  );

  toggleFilter(column: TableColumn, event: MouseEvent): void {
    event.stopPropagation();

    if (!column.filter) {
      return;
    }

    if (this.openFilter() === column.key) {
      this.closeFilter();
      return;
    }

    const button = event.currentTarget as HTMLElement;
    const header = button.closest('th');

    if (!header) return;

    const rect = header.getBoundingClientRect();
    const hostRect = this.host.nativeElement.getBoundingClientRect();

    const margin = 12;
    const gap = 4;

    const panelWidth = Math.min(290, window.innerWidth - margin * 2);

    const left = Math.max(
      margin,
      Math.min(rect.left, window.innerWidth - panelWidth - margin),
    );

    const top = rect.bottom + gap;

    // El panel absoluto usa la tabla como referencia, no la ventana.
    this.panelPosition.set({
      top: top - hostRect.top,
      left: left - hostRect.left,
    });

    this.openFilter.set(column.key);
  }

  applyFilter(value: TableFilterValue): void {
    const key = this.openFilter();
    if (key === null) return;
    this.filterChange.emit({ key, value });
    this.closeFilter();
  }

  closeFilter(): void {
    this.openFilter.set(null);
  }

  @HostListener('document:click')
  onOutsideClick(): void {
    this.closeFilter();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closeFilter();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.closeFilter();
  }
}
