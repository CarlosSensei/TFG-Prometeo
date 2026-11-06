import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { QuoteItem } from '../../models/quote.model';
@Component({
  selector: 'app-quote-detail-items',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './quote-detail-items.html',
  styleUrl: './quote-detail-items.scss',
})
export class QuoteDetailItems {
  readonly items = input<QuoteItem[] | null | undefined>([]);
}
