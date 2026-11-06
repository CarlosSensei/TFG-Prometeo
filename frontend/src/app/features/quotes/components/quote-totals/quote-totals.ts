import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { QuoteTotalsValue } from '../../models/quote-line-draft.model';

@Component({
  selector: 'app-quote-totals',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './quote-totals.html',
  styleUrl: './quote-totals.scss',
})
export class QuoteTotals {
  readonly totals = input.required<QuoteTotalsValue>();
  readonly estimated = input(false);
  readonly taxRate = input(0);
}
