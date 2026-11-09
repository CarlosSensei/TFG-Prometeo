import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { OrderItem } from '../../models/order.model';

@Component({
  selector: 'app-order-detail-items',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './order-detail-items.html',
  styleUrl: './order-detail-items.scss'
})
export class OrderDetailItems {

  readonly items = input<
    OrderItem[] | null | undefined
  >([]);
}
