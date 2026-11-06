import { Component, input, output } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { QuoteLineDraft } from '../../models/quote-line-draft.model';
import { calculateLine } from '../../utils/quote-calculations';
@Component({
  selector: 'app-quote-lines-editor',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './quote-lines-editor.html',
  styleUrl: './quote-lines-editor.scss',
})
export class QuoteLinesEditor {
  readonly lines = input.required<QuoteLineDraft[]>();
  readonly removed = output<number>();
  readonly quantityChanged = output<{ key: number; value: string }>();
  readonly discountChanged = output<{ key: number; value: string }>();
  readonly calculateLine = calculateLine;
}
