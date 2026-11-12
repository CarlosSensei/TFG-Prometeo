import { Component, effect, inject, input, output, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Drawer } from '../../../../shared/ui/drawer/drawer';
import { Button } from '../../../../shared/ui/button/button';
import { Badge } from '../../../../shared/ui/badge/badge';
import { ClientApiService } from '../../services/client-api.service';
import { Client } from '../../models/client.model';
import { ClientSummary } from '../client-summary/client-summary';
import { ClientTimeline } from '../client-timeline/client-timeline';
import { ClientQuotes } from '../client-quotes/client-quotes';
import { ClientOrders } from '../client-orders/client-orders';
import { ClientFollowups } from '../client-followups/client-followups';

type ClientTab = 'summary' | 'timeline' | 'quotes' | 'orders' | 'followups';

@Component({
  selector: 'app-client-detail-drawer',
  standalone: true,
  imports: [
    Drawer,
    Button,
    Badge,
    ClientSummary,
    ClientTimeline,
    ClientQuotes,
    ClientOrders,
    ClientFollowups
  ],
  templateUrl: './client-detail-drawer.html',
  styleUrl: './client-detail-drawer.scss',
})
export class ClientDetailDrawer {
  private readonly api = inject(ClientApiService);
  private readonly router = inject(Router);

  readonly clientId = input<number | null>(null);
  readonly closed = output<void>();
  readonly client = signal<Client | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly activeTab = signal<ClientTab>('summary');
  readonly changed = output<void>();
  readonly processing = signal(false);

  constructor() {
    effect((onCleanup) => {
      const id = this.clientId();
      this.client.set(null);
      this.error.set(null);
      this.loading.set(false);
      this.activeTab.set('summary');
      if (id === null) return;

      this.loading.set(true);
      const subscription = this.api.findById(id).subscribe({
        next: (client) => {
          this.client.set(client);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No se ha podido cargar la ficha del cliente.');
          this.loading.set(false);
        },
      });
      onCleanup(() => subscription.unsubscribe());
    });
  }

  close(): void {
    this.closed.emit();
  }

  selectTab(tab: ClientTab): void {
    this.activeTab.set(tab);
  }

  editClient(): void {
    const id = this.clientId();
    if (id === null) return;
    this.closed.emit();
    this.router.navigate(['/clients', id, 'edit']);
  }

  fullName(client: Client): string {
    return [client.name, client.surname, client.lastSurname]
      .filter(Boolean)
      .join(' ');
  }

  createQuote(): void {

    const client = this.client();

    if (!client?.active) {
      return;
    }

    const returnUrl = this.router.url;

    this.close();

    void this.router.navigate(['/quotes/new'], {
      queryParams: {
        clientId: client.id,
        returnUrl
      }
    });
  }

  createOrder(): void {

    const client = this.client();

    if (!client?.active) {
      return;
    }

    const returnUrl = this.router.url;

    this.close();

    void this.router.navigate(['/orders/new'], {
      queryParams: {
        clientId: client.id,
        returnUrl
      }
    });
  }

  deactivateClient(): void {

    const client = this.client();

    if (!client?.active || this.processing()) {
      return;
    }

    const confirmed = window.confirm(
      `¿Quieres desactivar a ${this.fullName(client)}?\n\n` +
      'El cliente dejará de aparecer en el listado de activos. ' +
      'Sus presupuestos, pedidos e historial se conservarán.'
    );

    if (!confirmed) return;

    this.processing.set(true);
    this.error.set(null);

    this.api.deactivate(client.id).subscribe({

      next: () => {

        this.processing.set(false);

        // Avisar al listado para que actualice sus datos.
        this.changed.emit();

        // Cerrar el drawer.
        this.close();
      },

      error: error => {

        console.error('Error desactivando cliente:', error);

        this.processing.set(false);

        this.error.set(
          'No se ha podido desactivar el cliente.'
        );
      }
    });
  }


}
