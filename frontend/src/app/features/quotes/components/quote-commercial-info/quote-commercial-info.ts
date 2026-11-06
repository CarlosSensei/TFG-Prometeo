import { Component, input, output } from '@angular/core';
import { Quote } from '../../models/quote.model';
import { SellerSummary } from '../../models/seller-summary.model';
import { Client } from '../../../clients/models/client.model';
import { Button } from '../../../../shared/ui/button/button';
import { formatQuoteDate } from '../../utils/quote-presentation';

@Component({
  selector: 'app-quote-commercial-info',
  standalone: true,
  imports: [Button],
  templateUrl: './quote-commercial-info.html',
  styleUrl: './quote-commercial-info.scss',
})
export class QuoteCommercialInfo {
  readonly quote = input.required<Quote>();
  readonly client = input<Client | null>(null);
  readonly seller = input<SellerSummary | null>(null);
  readonly clientRequested = output<number>();
  clientName(): string {
    const client = this.client();

    if (!client) {
      return 'Cliente no disponible';
    }

    return [client.name, client.surname, client.lastSurname]
      .filter(Boolean)
      .join(' ');
  }

  sellerName(): string {
    const seller = this.seller();

    if (!seller) {
      return 'Vendedor no disponible';
    }

    return [seller.firstName, seller.lastName].filter(Boolean).join(' ');
  }

  readonly formatDate = formatQuoteDate;
}
