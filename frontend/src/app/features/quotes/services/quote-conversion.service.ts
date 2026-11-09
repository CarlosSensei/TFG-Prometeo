import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ConvertedOrder {
  id: number;
  orderNumber: string;
  quoteId: number;
  clientId: number;
  status: string;
  total: number;
}

@Injectable({
  providedIn: 'root'
})
export class QuoteConversionService {

  private readonly http = inject(HttpClient);

  convert(quoteId: number): Observable<ConvertedOrder> {

    return this.http.post<ConvertedOrder>(`/api/orders/from-quote/${quoteId}`, {});
  }
}
