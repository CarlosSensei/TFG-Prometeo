import {
  Component,
  ElementRef,
  HostListener,
  inject,
  input,
  output,
  signal
} from '@angular/core';

export type FilterType =
  | 'text'
  | 'multi'
  | 'date'
  | 'number';

export interface FilterOption {
  value: string;
  label: string;
}

export interface TableFilterValue {
  text?: string;
  values?: string[];
  from?: string;
  to?: string;
  min?: number | null;
  max?: number | null;
}

export interface TableColumn {
  key: string;
  label: string;
  sortable?: boolean;
  filter?: {
    type: FilterType;
    options?: FilterOption[];
    placeholder?: string;
  };
}

export type TableRow = {
  id: string | number;
  [key: string]: string | number | boolean | null | undefined;
};

export interface TableActionEvent {
  action: string;
  row: TableRow;
}

export interface TableFilterEvent {
  key: string;
  value: TableFilterValue;
}

export type SortDirection = 'asc' | 'desc';

export interface TableSort {
  key: string;
  direction: SortDirection;
}


@Component({
  selector: 'app-data-table',
  templateUrl: './data-table.html',
  styleUrl: './data-table.scss'
})
export class DataTable {

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly columns = input.required<TableColumn[]>();
  readonly rows = input.required<TableRow[]>();
  readonly loading = input(false);

  readonly emptyMessage = input(
    'No se encontraron registros'
  );

  // Filtros aplicados desde el componente padre.
  readonly filters = input<Record<string, TableFilterValue>>({});

  readonly rowSelected = output<TableRow>();
  readonly rowAction = output<TableActionEvent>();
  readonly filterChange = output<TableFilterEvent>();

  // Estado del desplegable.
  readonly openFilter = signal<string | null>(null);
  readonly draft = signal<TableFilterValue>({});

  readonly panelPosition = signal({
    top: 0,
    left: 0
  });

  // Ordenación controlada desde el componente padre.
  readonly sort = input<TableSort | null>(null);

  // Evento emitido cuando cambia el criterio.
  readonly sortChange = output<TableSort>();

  // Cambiar ordenación de una columna.
  toggleSort(column: TableColumn): void {

    if (!column.sortable) {
      return;
    }

    const current = this.sort();

    const direction: SortDirection =
      current?.key === column.key &&
      current.direction === 'asc'
        ? 'desc'
        : 'asc';

    this.sortChange.emit({
      key: column.key,
      direction
    });
  }

  // Indicador visual de la ordenación.
  sortIcon(column: TableColumn): string {

    const current = this.sort();

    if (current?.key !== column.key) {
      return '↕';
    }

    return current.direction === 'asc'
      ? '↑'
      : '↓';
  }

  ariaSort(column: TableColumn):
    'ascending' | 'descending' | 'none' {

    const current = this.sort();

    if (current?.key !== column.key) {
      return 'none';
    }

    return current.direction === 'asc'
      ? 'ascending'
      : 'descending';
  }


  // ====================================
  // FILTROS
  // ====================================

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

    const panelWidth = Math.min(
      290,
      window.innerWidth - margin * 2
    );

    const left = Math.max(
      margin,
      Math.min(
        rect.left,
        window.innerWidth - panelWidth - margin
      )
    );

    const top = rect.bottom + gap;

    // El panel absoluto usa la tabla como referencia, no la ventana.
    this.panelPosition.set({
      top: top - hostRect.top,
      left: left - hostRect.left
    });

    const current = this.filters()[column.key] ?? {};

    this.draft.set({
      ...current,
      values: [...(current.values ?? [])]
    });

    this.openFilter.set(column.key);
  }

  currentColumn(): TableColumn | undefined {
    return this.columns().find(
      column => column.key === this.openFilter()
    );
  }

  setText(value: string): void {
    this.draft.update(draft => ({
      ...draft,
      text: value
    }));
  }

  setFrom(value: string): void {
    this.draft.update(draft => ({
      ...draft,
      from: value
    }));
  }

  setTo(value: string): void {
    this.draft.update(draft => ({
      ...draft,
      to: value
    }));
  }

  setMin(value: string): void {
    this.draft.update(draft => ({
      ...draft,
      min: value.trim() === '' ? null : Number(value)
    }));
  }

  setMax(value: string): void {
    this.draft.update(draft => ({
      ...draft,
      max: value.trim() === '' ? null : Number(value)
    }));
  }

  toggleOption(value: string, checked: boolean): void {

    this.draft.update(draft => {

      const values = draft.values ?? [];

      return {
        ...draft,
        values: checked
          ? [...new Set([...values, value])]
          : values.filter(item => item !== value)
      };
    });
  }

  applyFilter(): void {

    const key = this.openFilter();

    if (!key) {
      return;
    }

    const draft = this.draft();

    if (
      draft.from &&
      draft.to &&
      draft.from > draft.to
    ) {
      return;
    }

    if (
      draft.min != null &&
      draft.max != null &&
      draft.min > draft.max
    ) {
      return;
    }

    this.filterChange.emit({
      key,
      value: this.normalize(draft)
    });

    this.closeFilter();
  }

  activeFilters(): {
    key: string;
    label: string;
    description: string;
  }[] {

    return this.columns()
      .filter(column => this.hasFilter(column.key))
      .map(column => {

        const value = this.filters()[column.key];

        let description = '';

        switch (column.filter?.type) {

          case 'text':
            description = value.text ?? '';
            break;

          case 'multi':
            description = (value.values ?? [])
              .map(selected =>
                column.filter?.options?.find(
                  option => option.value === selected
                )?.label ?? selected
              )
              .join(', ');
            break;

          case 'date':
            description = [
              value.from ? `Desde ${value.from}` : '',
              value.to ? `Hasta ${value.to}` : ''
            ].filter(Boolean).join(' · ');
            break;

          case 'number':
            description = [
              value.min != null ? `Mín. ${value.min}` : '',
              value.max != null ? `Máx. ${value.max}` : ''
            ].filter(Boolean).join(' · ');
            break;
        }

        return {
          key: column.key,
          label: column.label,
          description
        };
      });
  }

  removeFilter(key: string): void {
    this.filterChange.emit({
      key,
      value: {}
    });
  }


  clearFilter(): void {

    const key = this.openFilter();

    if (!key) {
      return;
    }

    this.filterChange.emit({
      key,
      value: {}
    });

    this.closeFilter();
  }

  hasFilter(key: string): boolean {
    const filter = this.filters()[key];

    if (!filter) {
      return false;
    }

    return !!(
      filter.text?.trim() ||
      filter.values?.length ||
      filter.from ||
      filter.to ||
      filter.min != null ||
      filter.max != null
    );
  }

  private normalize(value: TableFilterValue): TableFilterValue {

    return {
      ...(value.text?.trim()
        ? { text: value.text.trim() }
        : {}),

      ...(value.values?.length
        ? { values: value.values }
        : {}),

      ...(value.from
        ? { from: value.from }
        : {}),

      ...(value.to
        ? { to: value.to }
        : {}),

      ...(value.min != null
        ? { min: value.min }
        : {}),

      ...(value.max != null
        ? { max: value.max }
        : {})
    };
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

  // ====================================
  // ACCIONES DE LA TABLA
  // ====================================

  selectRow(row: TableRow): void {
    this.rowSelected.emit(row);
  }

  executeAction(action: string, row: TableRow): void {
    this.rowAction.emit({ action, row });
  }
}
