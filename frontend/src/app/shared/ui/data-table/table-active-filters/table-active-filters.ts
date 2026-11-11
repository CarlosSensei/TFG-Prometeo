import { Component, computed, input, output } from '@angular/core';
import { TableColumn, TableFilterValue } from '../data-table.models';
import { hasTableFilter } from '../data-table.utils';

@Component({
  selector: 'app-table-active-filters',
  templateUrl: './table-active-filters.html',
  styleUrl: './table-active-filters.scss',
})
export class TableActiveFilters {
  readonly columns = input.required<TableColumn[]>();
  readonly filters = input<Record<string, TableFilterValue>>({});
  readonly filterRemoved = output<string>();

  readonly activeFilters = computed(() => {
    return this.columns()
      .filter((column) => hasTableFilter(this.filters()[column.key]))
      .map((column) => {
        const value = this.filters()[column.key];

        let description = '';

        switch (column.filter?.type) {
          case 'text':
            description = value.text ?? '';
            break;

          case 'multi':
            description = (value.values ?? [])
              .map(
                (selected) =>
                  column.filter?.options?.find(
                    (option) => option.value === selected,
                  )?.label ?? selected,
              )
              .join(', ');
            break;

          case 'date':
            description = [
              value.from ? `Desde ${value.from}` : '',
              value.to ? `Hasta ${value.to}` : '',
            ]
              .filter(Boolean)
              .join(' · ');
            break;

          case 'number':
            description = [
              value.min != null ? `Mín. ${value.min}` : '',
              value.max != null ? `Máx. ${value.max}` : '',
            ]
              .filter(Boolean)
              .join(' · ');
            break;
        }

        return {
          key: column.key,
          label: column.label,
          description,
        };
      });
  });
}
