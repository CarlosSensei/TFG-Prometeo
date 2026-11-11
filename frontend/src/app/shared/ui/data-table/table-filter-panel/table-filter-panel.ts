import { Component, OnChanges, input, output, signal } from '@angular/core';
import { TableColumn, TableFilterValue } from '../data-table.models';

@Component({
  selector: 'div[appTableFilterPanel]',
  templateUrl: './table-filter-panel.html',
  styleUrl: './table-filter-panel.scss',
  host: {
    role: 'group',
    '[attr.aria-label]': "'Filtro de ' + column().label",
    '(click)': '$event.stopPropagation()',
  },
})
export class TableFilterPanel implements OnChanges {
  readonly column = input.required<TableColumn>();
  readonly value = input<TableFilterValue>({});
  readonly applied = output<TableFilterValue>();
  readonly cleared = output<void>();
  readonly closed = output<void>();
  readonly draft = signal<TableFilterValue>({});

  ngOnChanges(): void {
    const current = this.value();
    this.draft.set({ ...current, values: [...(current.values ?? [])] });
  }

  setText(value: string): void {
    this.draft.update((draft) => ({
      ...draft,
      text: value,
    }));
  }

  setFrom(value: string): void {
    this.draft.update((draft) => ({
      ...draft,
      from: value,
    }));
  }

  setTo(value: string): void {
    this.draft.update((draft) => ({
      ...draft,
      to: value,
    }));
  }

  setMin(value: string): void {
    this.draft.update((draft) => ({
      ...draft,
      min: value.trim() === '' ? null : Number(value),
    }));
  }

  setMax(value: string): void {
    this.draft.update((draft) => ({
      ...draft,
      max: value.trim() === '' ? null : Number(value),
    }));
  }

  toggleOption(value: string, checked: boolean): void {
    this.draft.update((draft) => {
      const values = draft.values ?? [];

      return {
        ...draft,
        values: checked
          ? [...new Set([...values, value])]
          : values.filter((item) => item !== value),
      };
    });
  }

  applyFilter(): void {
    const draft = this.draft();

    if (draft.from && draft.to && draft.from > draft.to) {
      return;
    }

    if (draft.min != null && draft.max != null && draft.min > draft.max) {
      return;
    }

    this.applied.emit(this.normalize(draft));
  }

  private normalize(value: TableFilterValue): TableFilterValue {
    return {
      ...(value.text?.trim() ? { text: value.text.trim() } : {}),

      ...(value.values?.length ? { values: value.values } : {}),

      ...(value.from ? { from: value.from } : {}),

      ...(value.to ? { to: value.to } : {}),

      ...(value.min != null ? { min: value.min } : {}),

      ...(value.max != null ? { max: value.max } : {}),
    };
  }
}
