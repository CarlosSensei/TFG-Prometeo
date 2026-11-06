import {
  Component,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SellerSummary } from '../../models/seller-summary.model';
import { QuoteCommercialInfo } from '../../components/quote-commercial-info/quote-commercial-info';
import { QuoteDetailItems } from '../../components/quote-detail-items/quote-detail-items';
import { QuoteTotals } from '../../components/quote-totals/quote-totals';

import { forkJoin, of, catchError, switchMap, map } from 'rxjs';
import { Quote } from '../../models/quote.model';
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
    QuoteTotals,
  ],
  templateUrl: './quote-detail-drawer.html',
  styleUrl: './quote-detail-drawer.scss',
})
export class QuoteDetailDrawer {
  private readonly quoteApi = inject(QuoteApiService);
  private readonly clientApi = inject(ClientApiService);
  private readonly http = inject(HttpClient);

  readonly quoteId = input<number | null>(null);

  readonly closed = output<void>();
  readonly clientRequested = output<number>();

  readonly quote = signal<Quote | null>(null);
  readonly client = signal<Client | null>(null);
  readonly seller = signal<SellerSummary | null>(null);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

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

      const subscription = this.quoteApi
        .getById(id)
        .pipe(
          switchMap((quote) => {
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
        .subscribe({
          next: (data) => {
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
}
