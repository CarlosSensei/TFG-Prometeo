import { Component, computed, input, output } from '@angular/core';
import { Quote } from '../../models/quote.model';
import {
  DataTable,
  TableColumn,
  TableRow,
  TableFilterValue,
  TableSort,
  TableFilterEvent,
  TableActionEvent,
} from '../../../../shared/ui/data-table/data-table';
import { Pagination } from '../../../../shared/ui/pagination/pagination';
import { Card } from '../../../../shared/ui/card/card';
import { Button } from '../../../../shared/ui/button/button';
import {
  QUOTE_STATUS_OPTIONS,
  formatQuoteStatus,
  formatQuoteDate,
} from '../../utils/quote-presentation';

@Component({
  selector: 'app-quote-table',
  standalone: true,
  imports: [DataTable, Pagination, Card, Button],
  templateUrl: './quote-table.html',
  styleUrl: './quote-table.scss',
})
export class QuoteTable {
  readonly quotes = input.required<Quote[]>();
  readonly loading = input(false);
  readonly filters = input.required<Record<string, TableFilterValue>>();
  readonly sort = input.required<TableSort>();
  readonly page = input(0);
  readonly pageSize = input(10);
  readonly totalElements = input(0);
  readonly filtersCleared = output<void>();
  readonly filterChanged = output<TableFilterEvent>();
  readonly sortChanged = output<TableSort>();
  readonly rowAction = output<TableActionEvent>();
  readonly pageChanged = output<number>();
  readonly statusOptions = QUOTE_STATUS_OPTIONS;

  readonly columns: TableColumn[] = [
    {
      key: 'quoteNumber',
      label: 'Presupuesto',
      sortable: true,
      filter: {
        type: 'text',
        placeholder: 'Número de presupuesto',
      },
    },
    {
      key: 'quoteDate',
      label: 'Fecha',
      sortable: true,
      filter: {
        type: 'date',
      },
    },
    {
      key: 'expirationDate',
      label: 'Caducidad',
      sortable: true,
      filter: {
        type: 'date',
      },
    },
    {
      key: 'status',
      label: 'Estado',
      sortable: true,
      filter: {
        type: 'multi',
        options: this.statusOptions,
      },
    },
    {
      key: 'total',
      label: 'Importe',
      sortable: true,
      filter: {
        type: 'number',
      },
    },
    {
      key: 'actions',
      label: 'Acciones',
      sortable: false,
    },
  ];

  readonly tableRows = computed<TableRow[]>(() =>
    this.quotes().map((quote) => ({
      id: quote.id,
      quoteNumber: quote.quoteNumber,
      quoteDate: this.formatDate(quote.quoteDate),
      expirationDate: this.formatDate(quote.expirationDate),
      status: this.formatStatus(quote.status),
      total: this.formatMoney(quote.total),
    })),
  );

  readonly formatStatus = formatQuoteStatus;
  readonly formatDate = formatQuoteDate;

  formatMoney(value: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
    }).format(value);
  }
}
