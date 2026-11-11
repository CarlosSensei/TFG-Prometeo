import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DataTable, TableColumn, TableFilterEvent } from './data-table';

const columns: TableColumn[] = [
  { key: 'name', label: 'Nombre', sortable: true, filter: { type: 'text' } },
  {
    key: 'status',
    label: 'Estado',
    filter: {
      type: 'multi',
      options: [
        { value: 'active', label: 'Activo' },
        { value: 'pending', label: 'Pendiente' },
      ],
    },
  },
  { key: 'amount', label: 'Importe', filter: { type: 'number' } },
  { key: 'date', label: 'Fecha', filter: { type: 'date' } },
  { key: 'actions', label: 'Acciones' },
];

describe('DataTable', () => {
  let fixture: ComponentFixture<DataTable>;
  let root: HTMLElement;

  function click(selector: string): void {
    const button = root.querySelector<HTMLElement>(selector);
    expect(button).not.toBeNull();
    button!.click();
    fixture.detectChanges();
  }

  function openFilter(label: string): void {
    click(`[aria-label="Filtrar por ${label}"]`);
  }

  function enter(selector: string, value: string, event = 'input'): void {
    const field = root.querySelector<HTMLInputElement>(selector)!;
    field.value = value;
    field.dispatchEvent(new Event(event, { bubbles: true }));
    fixture.detectChanges();
  }

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [DataTable] });
    fixture = TestBed.createComponent(DataTable);
    fixture.componentRef.setInput('columns', columns);
    fixture.componentRef.setInput('rows', [{ id: 1, name: 'Ana' }]);
    fixture.detectChanges();
    root = fixture.nativeElement;
  });

  it('preserves table structure and forwards row selection and actions', () => {
    const selected = vi.fn();
    const action = vi.fn();
    fixture.componentInstance.rowSelected.subscribe(selected);
    fixture.componentInstance.rowAction.subscribe(action);
    expect(root.querySelector('table > thead > tr > th')).not.toBeNull();
    expect(root.querySelector('table > tbody > tr > td')).not.toBeNull();
    click('.cell-button');
    click('.table-action');
    expect(selected).toHaveBeenCalledWith({ id: 1, name: 'Ana' });
    expect(action).toHaveBeenCalledWith({
      action: 'view',
      row: { id: 1, name: 'Ana' },
    });
  });

  it('emits controlled sorting and reflects its accessible state', () => {
    const sorted = vi.fn();
    fixture.componentInstance.sortChange.subscribe(sorted);
    click('.column-sort-trigger');
    expect(sorted).toHaveBeenLastCalledWith({ key: 'name', direction: 'asc' });
    fixture.componentRef.setInput('sort', { key: 'name', direction: 'asc' });
    fixture.detectChanges();
    expect(root.querySelector('th')!.getAttribute('aria-sort')).toBe(
      'ascending',
    );
    click('.column-sort-trigger');
    expect(sorted).toHaveBeenLastCalledWith({ key: 'name', direction: 'desc' });
  });

  it('normalizes draft text and only changes filters on apply', () => {
    const changed = vi.fn();
    fixture.componentInstance.filterChange.subscribe(changed);
    openFilter('Nombre');
    enter('input[type="search"]', '  Ana  ');
    expect(changed).not.toHaveBeenCalled();
    click('.filter-apply');
    expect(changed).toHaveBeenCalledWith({
      key: 'name',
      value: { text: 'Ana' },
    });
    expect(root.querySelector('.column-filter-panel')).toBeNull();
  });

  it('discards drafts when changing columns and preserves applied multi selections', () => {
    const filters = { status: { values: ['active'] } };
    fixture.componentRef.setInput('filters', filters);
    fixture.detectChanges();
    const changed = vi.fn();
    fixture.componentInstance.filterChange.subscribe(changed);
    openFilter('Estado');
    const options = root.querySelectorAll<HTMLInputElement>(
      'input[type="checkbox"]',
    );
    expect(options[0].checked).toBe(true);
    options[1].click();
    fixture.detectChanges();
    expect(filters.status.values).toEqual(['active']);
    openFilter('Nombre');
    expect(
      root.querySelector<HTMLInputElement>('input[type="search"]')!.value,
    ).toBe('');
    openFilter('Estado');
    expect(
      root.querySelectorAll<HTMLInputElement>('input[type="checkbox"]')[1]
        .checked,
    ).toBe(false);
    click('.filter-apply');
    expect(changed).toHaveBeenCalledWith({
      key: 'status',
      value: { values: ['active'] },
    });
  });

  it('prevents invalid number and date ranges from being applied', () => {
    const changed = vi.fn();
    fixture.componentInstance.filterChange.subscribe(changed);
    openFilter('Importe');
    enter('input[placeholder="Desde"]', '10');
    enter('input[placeholder="Hasta"]', '5');
    click('.filter-apply');
    expect(changed).not.toHaveBeenCalled();
    expect(root.querySelector('.filter-error')).not.toBeNull();
    enter('input[placeholder="Hasta"]', '20');
    click('.filter-apply');
    expect(changed).toHaveBeenCalledWith({
      key: 'amount',
      value: { min: 10, max: 20 },
    });
    changed.mockClear();
    openFilter('Fecha');
    const dates = root.querySelectorAll<HTMLInputElement>('input[type="date"]');
    dates[0].value = '2026-11-12';
    dates[0].dispatchEvent(new Event('change'));
    dates[1].value = '2026-11-11';
    dates[1].dispatchEvent(new Event('change'));
    fixture.detectChanges();
    click('.filter-apply');
    expect(changed).not.toHaveBeenCalled();
    expect(root.querySelector('.filter-error')).not.toBeNull();
  });

  it('removes active filters and clears the open filter through the public output', () => {
    fixture.componentRef.setInput('filters', {
      status: { values: ['active'] },
    });
    fixture.detectChanges();
    const changes: TableFilterEvent[] = [];
    fixture.componentInstance.filterChange.subscribe((change) =>
      changes.push(change),
    );
    expect(root.querySelector('.filter-chip')!.textContent).toContain('Activo');
    click('.filter-chip');
    openFilter('Estado');
    click('.filter-clear');
    expect(changes).toEqual([
      { key: 'status', value: {} },
      { key: 'status', value: {} },
    ]);
    expect(root.querySelector('.column-filter-panel')).toBeNull();
  });

  it('closes the panel on escape, outside clicks, resize and table scroll', () => {
    const dismissals = [
      () =>
        document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' })),
      () => document.dispatchEvent(new MouseEvent('click')),
      () => window.dispatchEvent(new Event('resize')),
      () =>
        root.querySelector('.table-scroll')!.dispatchEvent(new Event('scroll')),
    ];
    for (const dismiss of dismissals) {
      openFilter('Nombre');
      expect(root.querySelector('.column-filter-panel')).not.toBeNull();
      click('.filter-panel-heading');
      expect(root.querySelector('.column-filter-panel')).not.toBeNull();
      dismiss();
      fixture.detectChanges();
      expect(root.querySelector('.column-filter-panel')).toBeNull();
    }
  });

  it('renders loading and empty states spanning every column', () => {
    fixture.componentRef.setInput('loading', true);
    fixture.detectChanges();
    expect(root.querySelector('[role="status"]')!.textContent).toContain(
      'Cargando registros',
    );
    expect(root.querySelector('td')!.colSpan).toBe(columns.length);
    fixture.componentRef.setInput('loading', false);
    fixture.componentRef.setInput('rows', []);
    fixture.componentRef.setInput('emptyMessage', 'Sin clientes');
    fixture.detectChanges();
    expect(root.querySelector('.empty-cell')!.textContent).toContain(
      'Sin clientes',
    );
  });
});
