import { Component, effect, inject, input, output, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CurrencyPipe } from '@angular/common';
import { forkJoin, of, catchError, switchMap, map } from 'rxjs';
import { Quote, QuoteStatus } from '../../models/quote.model';
import { QuoteApiService } from '../../services/quote-api.service';
import { ClientApiService } from '../../../clients/services/client-api.service';
import { Client } from '../../../clients/models/client.model';
import { Drawer } from '../../../../shared/ui/drawer/drawer';
import { Button } from '../../../../shared/ui/button/button';
import { Badge, BadgeVariant } from '../../../../shared/ui/badge/badge';

interface SellerSummary {
  id: number;
  firstName: string;
  lastName: string;
}

@Component({
  selector: 'app-quote-detail-drawer',
  standalone: true,
  imports: [
    Drawer,
    Button,
    Badge,
    CurrencyPipe
  ],
  templateUrl: './quote-detail-drawer.html',
  styleUrl: './quote-detail-drawer.scss'
})
export class QuoteDetailDrawer {

  // =====================================
  // DEPENDENCIAS
  // =====================================

  private readonly quoteApi = inject(QuoteApiService);
  private readonly clientApi = inject(ClientApiService);
  private readonly http = inject(HttpClient);

  // =====================================
  // INPUTS / OUTPUTS
  // =====================================

  readonly quoteId = input<number | null>(null);

  readonly closed = output<void>();
  readonly clientRequested = output<number>();

  // =====================================
  // ESTADO
  // =====================================

  readonly quote = signal<Quote | null>(null);
  readonly client = signal<Client | null>(null);
  readonly seller = signal<SellerSummary | null>(null);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  // =====================================
  // CARGA DEL DETALLE
  // =====================================

  constructor() {

    effect((onCleanup) => {

      const id = this.quoteId();

      this.quote.set(null);
      this.client.set(null);
      this.seller.set(null);
      this.error.set(null);

      if (id === null) {
        this.loading.set(false);
        return;
      }

      this.loading.set(true);

      const subscription = this.quoteApi.getById(id).pipe(

        switchMap(quote => {

          // Consultas auxiliares:
          // los fallos al recuperar nombres no deben
          // impedir ver el presupuesto.

          const clientRequest = this.clientApi
            .getById(quote.clientId)
            .pipe(
              catchError(() => of(null))
            );

          const sellerRequest = quote.sellerId != null
            ? this.http.get<SellerSummary>(
                `/api/users/${quote.sellerId}`
              ).pipe(
                catchError(() => of(null))
              )
            : of(null);

          return forkJoin({
            client: clientRequest,
            seller: sellerRequest
          }).pipe(
            map(data => ({
              quote,
              client: data.client,
              seller: data.seller
            }))
          );
        })

      ).subscribe({

        next: data => {

          this.quote.set(data.quote);
          this.client.set(data.client);
          this.seller.set(data.seller);

          this.loading.set(false);
        },

        error: err => {

          console.error(
            'Error cargando detalle del presupuesto:',
            err
          );

          this.error.set(
            'No se ha podido cargar el presupuesto.'
          );

          this.loading.set(false);
        }

      });

      onCleanup(() => subscription.unsubscribe());
    });
  }

  // =====================================
  // NAVEGACIÓN
  // =====================================

  close(): void {
    this.closed.emit();
  }

  openClient(): void {

    const clientId = this.quote()?.clientId;

    if (clientId == null) {
      return;
    }

    this.clientRequested.emit(clientId);
  }

  // =====================================
  // PRESENTACIÓN
  // =====================================

  clientName(): string {

    const client = this.client();

    if (!client) {
      return 'Cliente no disponible';
    }

    return [
      client.name,
      client.surname,
      client.lastSurname
    ]
      .filter(Boolean)
      .join(' ');
  }

  sellerName(): string {

    const seller = this.seller();

    if (!seller) {
      return 'Vendedor no disponible';
    }

    return [
      seller.firstName,
      seller.lastName
    ]
      .filter(Boolean)
      .join(' ');
  }

  formatDate(value: string | null | undefined): string {

    if (!value) {
      return '—';
    }

    const [year, month, day] = value.split('-');

    return `${day}/${month}/${year}`;
  }

  formatStatus(status: QuoteStatus): string {

    const labels: Record<QuoteStatus, string> = {
      DRAFT: 'Borrador',
      SENT: 'Enviado',
      ACCEPTED: 'Aceptado',
      REJECTED: 'Rechazado',
      EXPIRED: 'Caducado',
      CONVERTED: 'Convertido'
    };

    return labels[status];
  }

  statusVariant(status: QuoteStatus): BadgeVariant {

    const variants: Record<QuoteStatus, BadgeVariant> = {
      DRAFT: 'neutral',
      SENT: 'info',
      ACCEPTED: 'success',
      REJECTED: 'danger',
      EXPIRED: 'warning',
      CONVERTED: 'success'
    };

    return variants[status];
  }

  readonly editRequested = output<number>();

  editQuote(): void {
    const current = this.quote();

    if (current?.status === 'DRAFT' && current.active) {
      this.editRequested.emit(current.id);
    }
  }


}
