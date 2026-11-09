import { Component, computed, input, output } from '@angular/core';
import { Order } from '../../models/order.model';
import { Client } from '../../../clients/models/client.model';
import { SellerSummary } from '../../../quotes/models/seller-summary.model';
import { Button } from '../../../../shared/ui/button/button';
import { formatOrderDate } from '../../utils/order-presentation';

@Component({
  selector: 'app-order-commercial-info',
  standalone: true,
  imports: [Button],
  templateUrl: './order-commercial-info.html',
  styleUrl: './order-commercial-info.scss'
})
export class OrderCommercialInfo {

  readonly order = input.required<Order>();
  readonly client = input<Client | null>(null);
  readonly seller = input<SellerSummary | null>(null);
  readonly clientRequested = output<number>();
  readonly clientName = computed(() => {

    const client = this.client();

    if (!client) {
      return `Cliente #${this.order().clientId}`;
    }

    return [
      client.name,
      client.surname,
      client.lastSurname
    ].filter(Boolean).join(' ');
  });

  readonly sellerName = computed(() => {

    const seller = this.seller();

    if (!seller) {
      return 'No disponible';
    }

    return [seller.firstName, seller.lastName].filter(Boolean).join(' ');});

  readonly formatDate = formatOrderDate;
}
