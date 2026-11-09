import { Component, input, output } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { CommercialLineDraft } from '../../models/commercial-line.model';
import { calculateLine } from '../../utils/commercial-calculations';
@Component({
  selector: 'app-commercial-lines-editor',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './commercial-lines-editor.html',
  styleUrl: './commercial-lines-editor.scss',
})
export class CommercialLinesEditor {
  readonly lines = input.required<CommercialLineDraft[]>();
  readonly removed = output<number>();
  readonly quantityChanged = output<{ key: number; value: string }>();
  readonly discountChanged = output<{ key: number; value: string }>();
  readonly calculateLine = calculateLine;
}
