import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { CommercialTotals } from '../../models/commercial-line.model';

@Component({
  selector: 'app-commercial-totals',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './commercial-totals.html',
  styleUrl: './commercial-totals.scss',
})
export class CommercialTotalsComponent {
  readonly totals = input.required<CommercialTotals>();
  readonly estimated = input(false);
  readonly taxRate = input(0);
}
