
import { Component, signal } from '@angular/core';
import { Input } from '../../../../shared/ui/input/input';
import { Select, SelectOption } from '../../../../shared/ui/select/select';
import { DataTable, TableColumn, TableRow } from '../../../../shared/ui/data-table/data-table';
import { Pagination } from '../../../../shared/ui/pagination/pagination';

@Component({
  selector: 'app-ui-playground',
  standalone: true,
  imports: [Input, Select, DataTable, Pagination],
  templateUrl: './ui-playground.html',
  styleUrl: './ui-playground.scss'
})
export class UiPlayground {
  readonly search = signal('');
  readonly status = signal('');
  readonly page = signal(0);
  readonly size = 3;

  readonly statusOptions: SelectOption[] = [
    { value: '', label: 'Todos los estados' },
    { value: 'Activo', label: 'Activo' },
    { value: 'Inactivo', label: 'Inactivo' }
  ];

  readonly columns: TableColumn[] = [
    { key: 'name', label: 'Cliente' },
    { key: 'email', label: 'Email' },
    { key: 'status', label: 'Estado' }
  ];

  readonly clients: TableRow[] = [
    { id: 1, name: 'María García', email: 'maria@example.com', status: 'Activo' },
    { id: 2, name: 'Juan Pérez', email: 'juan@example.com', status: 'Activo' },
    { id: 3, name: 'Laura Martín', email: 'laura@example.com', status: 'Inactivo' },
    { id: 4, name: 'Pedro López', email: 'pedro@example.com', status: 'Activo' },
    { id: 5, name: 'Ana Ruiz', email: 'ana@example.com', status: 'Activo' }
  ];

  readonly filtered = () => {
    const query = this.search().toLowerCase().trim();

    return this.clients.filter(client =>
      String(client['name']).toLowerCase().includes(query) &&
      (!this.status() || client['status'] === this.status())
    );
  };

  readonly pageRows = () =>
    this.filtered().slice(
      this.page() * this.size,
      (this.page() + 1) * this.size
    );

  onSearch(value: string): void {
    this.search.set(value);
    this.page.set(0);
  }

  onStatus(value: string): void {
    this.status.set(value);
    this.page.set(0);
  }

  onPageChange(page: number): void {
    this.page.set(page);
  }

  onRowSelected(row: TableRow): void {
    console.info('Registro seleccionado:', row.id);
  }
}
