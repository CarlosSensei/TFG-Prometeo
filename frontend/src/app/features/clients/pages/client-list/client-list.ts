import { Component, DestroyRef, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ClientApiService } from '../../services/client-api.service';
import { Client } from '../../models/client.model';
import { DataTable, TableColumn, TableRow, TableActionEvent } from '../../../../shared/ui/data-table/data-table';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { Card } from '../../../../shared/ui/card/card';
import { Badge } from '../../../../shared/ui/badge/badge';
import { Button } from '../../../../shared/ui/button/button';
import { ClientDetailDrawer } from
  '../../component/client-detail-drawer/client-detail-drawer';
import { Router } from '@angular/router';


@Component({
  selector: 'app-client-list',
  standalone: true,
  imports: [DataTable, Pagination, Card, Badge, Button, ClientDetailDrawer],
  templateUrl: './client-list.html',
  styleUrl: './client-list.scss'
})
export class ClientList {

  private readonly clientApi = inject(ClientApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);

  readonly clients = signal<Client[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly page = signal(0);
  readonly size = 10;
  readonly totalElements = signal(0);
  readonly selectedClientId = signal<number | null>(null);
  readonly detailClientId = signal<number | null>(null);

  readonly columns: TableColumn[] = [
    { key: 'name', label: 'Cliente' },
    { key: 'dni', label: 'DNI / NIE' },
    { key: 'email', label: 'Email' },
    { key: 'phoneNumber', label: 'Teléfono' },
    { key: 'city', label: 'Ciudad' },
    { key: 'actions', label: 'Acciones' }
  ];

  constructor() {
    this.loadClients();
  }

  get tableRows(): TableRow[] {
    return this.clients().map(client => ({
      id: client.id,
      name: [
        client.name,
        client.surname,
        client.lastSurname
      ].filter(Boolean).join(' '),
      dni: client.dni,
      email: client.email ?? '—',
      phoneNumber: client.phoneNumber ?? '—',
      city: client.city ?? '—',
      status: client.active ? 'Activo' : 'Inactivo'
    }));
  }

  handleRowAction(event: TableActionEvent): void {
    if (event.action !== 'view') return;

    const id = Number(event.row.id);

    if (!Number.isSafeInteger(id) || id <= 0) return;

    this.openClientDetail(id);
  }

  loadClients(): void {
    this.loading.set(true);
    this.error.set(null);

    this.clientApi
      .findAll(this.page(), this.size)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: response => {
          this.clients.set(response.content);
          this.totalElements.set(response.totalElements);
          this.page.set(response.number);
        },
        error: err => {
          console.error('Error cargando clientes:', err);
          this.clients.set([]);
          this.totalElements.set(0);

          this.error.set(
            'No se pudieron cargar los clientes. Comprueba la conexión con el servidor.'
          );
        }
      });
  }

  changePage(page: number): void {
    this.page.set(page);
    this.loadClients();
  }

  selectClient(row: TableRow): void {
    this.selectedClientId.set(Number(row.id));
  }

  createClient(): void {
    this.router.navigate(['/clients/new']);
  }

  openClientDetail(id: number): void {
    this.detailClientId.set(id);
  }

  closeClientDetail(): void {
    this.detailClientId.set(null);
  }

  editSelectedClient(): void {
    const id = this.selectedClientId();

    if (id === null) return;

    this.router.navigate(['/clients', id, 'edit']);
  }

  retry(): void {
    this.loadClients();
  }
}
