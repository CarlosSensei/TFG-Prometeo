import { Component, effect, inject, input, output, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { SellerSummary } from '../../models/seller-summary.model';
import { QuoteCommercialInfo } from '../../components/quote-commercial-info/quote-commercial-info';
import { QuoteDetailItems } from '../../components/quote-detail-items/quote-detail-items';
import { CommercialTotalsComponent } from '../../../../shared/commercial/components/commercial-totals/commercial-totals';
import { QuoteConversionService } from '../../services/quote-conversion.service';
import { forkJoin, of, catchError, switchMap, map, finalize } from 'rxjs';
import { Quote } from '../../models/quote.model';
import { QuoteFollowUpForm } from '../../components/quote-followup-form/quote-followup-form';
import { QuoteFollowUpList } from '../../components/quote-followup-list/quote-followup-list';
import { FollowUp } from '../../../followups/models/followup.model';
import { QuoteApiService } from '../../services/quote-api.service';
import { ClientApiService } from '../../../clients/services/client-api.service';
import { Client } from '../../../clients/models/client.model';
import { Drawer } from '../../../../shared/ui/drawer/drawer';
import { Button } from '../../../../shared/ui/button/button';
import { Badge } from '../../../../shared/ui/badge/badge';

import {
  formatQuoteDate,
  formatQuoteStatus,
  quoteStatusVariant,
} from '../../utils/quote-presentation';

@Component({
  selector: 'app-quote-detail-drawer',
  standalone: true,
  imports: [
    Drawer,
    Button,
    Badge,
    QuoteCommercialInfo,
    QuoteDetailItems,
    QuoteFollowUpForm,
    QuoteFollowUpList,
    CommercialTotalsComponent,
  ],
  templateUrl: './quote-detail-drawer.html',
  styleUrl: './quote-detail-drawer.scss',
})
export class QuoteDetailDrawer {
  private readonly quoteApi = inject(QuoteApiService);
  private readonly clientApi = inject(ClientApiService);
  private readonly http = inject(HttpClient);
  private readonly conversionApi = inject(QuoteConversionService);

  // Avisar al listado de que los datos han cambiado.
  readonly updated = output<void>();

  // Estado de la operación comercial.
  readonly processing = signal(false);

  readonly actionError = signal<string | null>(null);

  readonly actionSuccess = signal<string | null>(null);

  // Evita que una respuesta de una operación anterior
  // sobrescriba el detalle de otro presupuesto.
  private actionQuoteId: number | null = null;

  readonly quoteId = input<number | null>(null);

  readonly closed = output<void>();
  readonly clientRequested = output<number>();

  readonly quote = signal<Quote | null>(null);
  readonly client = signal<Client | null>(null);
  readonly seller = signal<SellerSummary | null>(null);

  readonly creatingFollowUp = signal(false);
  readonly followUpSuccess = signal<string | null>(null);
  readonly followUpRefreshToken = signal(0);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor() {
    effect((onCleanup) => {
      const id = this.quoteId();

      this.quote.set(null);
      this.client.set(null);
      this.seller.set(null);
      this.error.set(null);
      this.actionError.set(null);
      this.actionSuccess.set(null);
      this.processing.set(false);
      this.creatingFollowUp.set(false);
      this.followUpSuccess.set(null);

      if (id === null) {
        this.loading.set(false);
        return;
      }

      this.loading.set(true);

      const subscription = this.quoteApi
        .getById(id)
        .pipe(switchMap((quote) => {
            // Consultas auxiliares:
            // los fallos al recuperar nombres no deben
            // impedir ver el presupuesto.

            const clientRequest = this.clientApi
              .getById(quote.clientId)
              .pipe(catchError(() => of(null)));

            const sellerRequest =
              quote.sellerId != null
                ? this.http
                    .get<SellerSummary>(`/api/users/${quote.sellerId}`)
                    .pipe(catchError(() => of(null)))
                : of(null);

            return forkJoin({
              client: clientRequest,
              seller: sellerRequest,
            }).pipe(
              map((data) => ({
                quote,
                client: data.client,
                seller: data.seller,
              })),
            );
          }),
        )
        .subscribe({next: (data) => {
            this.quote.set(data.quote);
            this.client.set(data.client);
            this.seller.set(data.seller);

            this.loading.set(false);
          },

          error: (err) => {
            console.error('Error cargando detalle del presupuesto:', err);
            this.error.set('No se ha podido cargar el presupuesto.');

            this.loading.set(false);
          },
        });

      onCleanup(() => subscription.unsubscribe());
    });
  }

  close(): void {

    this.creatingFollowUp.set(false);
    this.closed.emit();
    
  }

  readonly formatDate = formatQuoteDate;
  readonly formatStatus = formatQuoteStatus;
  readonly statusVariant = quoteStatusVariant;

  readonly editRequested = output<number>();

  editQuote(): void {
    const current = this.quote();

    if (current?.status === 'DRAFT' && current.active) {
      this.editRequested.emit(current.id);
    }
  }

  sendQuote(): void {
    this.executeTransition('send');
  }

  acceptQuote(): void {
    this.executeTransition('accept');
  }

  rejectQuote(): void {
    this.executeTransition('reject');
  }

  private executeTransition(action: 'send' | 'accept' | 'reject'): void {

    const current = this.quote();

    if (!current || this.processing()) {
      return;
    }

    const allowed =
      (action === 'send' && current.status === 'DRAFT') ||
      (action === 'accept' && current.status === 'SENT') ||
      (action === 'reject' && current.status === 'SENT');

    if (!allowed || !current.active) {
      return;
    }

    if (action === 'reject') {

      const confirmed = window.confirm(
        '¿Confirmas que el cliente ha rechazado este presupuesto?'
      );

      if (!confirmed) {
        return;
      }
    }

    this.processing.set(true);
    this.actionError.set(null);
    this.actionSuccess.set(null);

    const request = {
      send: () => this.quoteApi.send(current.id),
      accept: () => this.quoteApi.accept(current.id),
      reject: () => this.quoteApi.reject(current.id)
    }[action]();

    request.subscribe({

      next: updatedQuote => {

        if (this.quoteId() !== current.id) {
          return;
        }

        this.quote.set(updatedQuote);

        this.processing.set(false);

        this.actionSuccess.set(action === 'send'
            ? 'Presupuesto marcado como enviado.' : action === 'accept'
              ? 'Presupuesto aceptado.' : 'Presupuesto rechazado.'
        );

        this.updated.emit();
      },

      error: (err: HttpErrorResponse) => {

        if (this.quoteId() !== current.id) {
          return;
        }

        console.error('Error cambiando estado del presupuesto:', err);

        this.actionError.set(err.error?.message ??
          'No se ha podido cambiar el estado del presupuesto.'
        );

        this.processing.set(false);
      }
    });
  }

  convertToOrder(): void {

    const current = this.quote();

    if (
      !current ||
      current.status !== 'ACCEPTED' ||
      !current.active ||
      this.processing()
    ) {
      return;
    }

    const confirmed = window.confirm(
      '¿Crear un pedido a partir de este presupuesto aceptado?'
    );

    if (!confirmed) {
      return;
    }

    this.processing.set(true);
    this.actionError.set(null);
    this.actionSuccess.set(null);

    this.conversionApi.convert(current.id)
      .subscribe({

        next: order => {

          if (this.quoteId() !== current.id) {
            return;
          }

          // El backend convierte el presupuesto
          // a CONVERTED dentro de la transacción.
          this.quote.update(quote => quote ? { ...quote, status: 'CONVERTED' } : null);

          this.actionSuccess.set(
            `Pedido ${order.orderNumber} creado correctamente.`
          );

          this.processing.set(false);

          // Refrescar QuoteList para mostrar
          // el nuevo estado comercial.
          this.updated.emit();
        },

        error: (err: HttpErrorResponse) => {

          if (this.quoteId() !== current.id) {
            return;
          }

          console.error('Error convirtiendo presupuesto:', err);

          this.actionError.set(err.error?.message ??
            'No se ha podido convertir el presupuesto.'
          );

          this.processing.set(false);
        }
      });
  }

  openFollowUpForm(): void {
    if (this.quote()?.active && !this.processing()) {
      this.followUpSuccess.set(null);
      this.creatingFollowUp.set(true);
    }
  }

  cancelFollowUpForm(): void {
    this.creatingFollowUp.set(false);
  }

  onFollowUpCreated(followUp: FollowUp): void {
    if (this.quoteId() !== followUp.quoteId) return;
    this.creatingFollowUp.set(false);
    this.followUpSuccess.set(`Seguimiento #${followUp.id} programado correctamente.`);
    this.followUpRefreshToken.update(value => value + 1);
  }

  onFollowUpChanged(): void {
    this.updated.emit();
  }

}
