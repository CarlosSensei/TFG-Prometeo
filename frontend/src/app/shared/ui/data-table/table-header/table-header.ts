import { Component, input, output } from '@angular/core';
import {
  SortDirection,
  TableColumn,
  TableFilterValue,
  TableSort,
} from '../data-table.models';
import { hasTableFilter } from '../data-table.utils';

@Component({
  selector: 'thead[appTableHeader]',
  templateUrl: './table-header.html',
  styleUrl: './table-header.scss',
})
export class TableHeader {
  readonly columns = input.required<TableColumn[]>();
  readonly filters = input<Record<string, TableFilterValue>>({});
  readonly sort = input<TableSort | null>(null);
  readonly openFilter = input<string | null>(null);
  readonly sortChange = output<TableSort>();
  readonly filterRequested = output<{
    column: TableColumn;
    event: MouseEvent;
  }>();

  hasFilter(key: string): boolean {
    return hasTableFilter(this.filters()[key]);
  }

  requestFilter(column: TableColumn, event: MouseEvent): void {
    this.filterRequested.emit({ column, event });
  }

  toggleSort(column: TableColumn): void {
    if (!column.sortable) {
      return;
    }

    const current = this.sort();

    const direction: SortDirection =
      current?.key === column.key && current.direction === 'asc'
        ? 'desc'
        : 'asc';

    this.sortChange.emit({
      key: column.key,
      direction,
    });
  }

  // Indicador visual de la ordenación.
  sortIcon(column: TableColumn): string {
    const current = this.sort();

    if (current?.key !== column.key) {
      return '↕';
    }

    return current.direction === 'asc' ? '↑' : '↓';
  }

  ariaSort(column: TableColumn): 'ascending' | 'descending' | 'none' {
    const current = this.sort();

    if (current?.key !== column.key) {
      return 'none';
    }

    return current.direction === 'asc' ? 'ascending' : 'descending';
  }
}
