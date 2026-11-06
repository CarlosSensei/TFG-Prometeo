import { Component, effect, inject, input, signal } from '@angular/core';
import {
  DataTable,
  TableColumn,
  TableRow,
} from '../../../../shared/ui/data-table/data-table';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { ClientDocumentsApiService } from '../../services/client-documents-api.service';
import { ClientQuote } from '../../models/client-documents.model';

@Component({
  selector: 'app-client-quotes',
  standalone: true,
  imports: [DataTable, Pagination],
  templateUrl: './client-quotes.html',
  styleUrl: './client-quotes.scss',
})
export class ClientQuotes {
  private readonly documentsApi = inject(ClientDocumentsApiService);
  readonly clientId = input.required<number>();
  readonly documentPageSize = 5;
  readonly quotePage = signal(0);
  readonly quotes = signal<ClientQuote[]>([]);
  readonly quotesTotal = signal(0);
  readonly documentsLoading = signal(false);
  readonly documentsError = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.clientId();
      this.quotePage.set(0);
    });
    effect((onCleanup) => {
      const id = this.clientId();
      const page = this.quotePage();
      this.quotes.set([]);
      this.quotesTotal.set(0);
      this.documentsError.set(null);
      this.documentsLoading.set(true);
      const subscription = this.documentsApi
        .getQuotes(id, page, this.documentPageSize)
        .subscribe({
          next: (response) => {
            this.quotes.set(response.content);
            this.quotesTotal.set(response.totalElements);
            this.documentsLoading.set(false);
          },
          error: () => {
            this.documentsError.set(
              'No se han podido recuperar los presupuestos.',
            );
            this.documentsLoading.set(false);
          },
        });
      onCleanup(() => subscription.unsubscribe());
    });
  }
  readonly quoteColumns: TableColumn[] = [
    { key: 'quoteNumber', label: 'Presupuesto' },
    { key: 'quoteDate', label: 'Fecha' },
    { key: 'status', label: 'Estado' },
    { key: 'total', label: 'Importe' },
  ];

  get quoteRows(): TableRow[] {
    return this.quotes().map((quote) => ({
      id: quote.id,
      quoteNumber: quote.quoteNumber,
      quoteDate: quote.quoteDate,
      status: this.formatStatus(quote.status),
      total: this.formatMoney(quote.total),
    }));
  }
  formatMoney(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  }

  formatStatus(status: string | null): string {
    return status?.replaceAll('_', ' ') || 'Sin estado';
  }

  changeQuotePage(page: number): void {
    this.quotePage.set(page);
  }
}
