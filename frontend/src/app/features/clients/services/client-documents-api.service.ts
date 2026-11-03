import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  ClientQuote,
  ClientOrder,
  PageResponse
} from '../models/client-documents.model';
import { ClientFollowUp, CompleteFollowUpPayload } from '../models/client-followup.model';

@Injectable({
  providedIn: 'root'
})
export class ClientDocumentsApiService {

  private readonly http = inject(HttpClient);

  // Mantener el mismo prefijo de API que ClientApiService.
  // Ejemplo: '/api' si Angular lo redirige mediante proxy.
  private readonly apiBase = '/api';

  getQuotes(
    clientId: number,
    page = 0,
    size = 5
  ): Observable<PageResponse<ClientQuote>> {

    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'quoteDate,desc');

    return this.http.get<PageResponse<ClientQuote>>(
      `${this.apiBase}/quotes/client/${clientId}`,
      { params }
    );
  }

  getOrders(
    clientId: number,
    page = 0,
    size = 5
  ): Observable<PageResponse<ClientOrder>> {

    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'orderDate,desc');

    return this.http.get<PageResponse<ClientOrder>>(
      `${this.apiBase}/orders/client/${clientId}`,
      { params }
    );
  }


  getFollowUps(
    clientId: number,
    page = 0,
    size = 5
  ): Observable<PageResponse<ClientFollowUp>> {

    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'scheduledAt,desc');

    return this.http.get<PageResponse<ClientFollowUp>>(
      `${this.apiBase}/clients/${clientId}/followups`,
      { params }
    );
  }

  completeFollowUp(
    followUpId: number,
    payload: CompleteFollowUpPayload
  ): Observable<ClientFollowUp> {

    return this.http.patch<ClientFollowUp>(
      `${this.apiBase}/followups/${followUpId}/complete`,
      payload
    );
  }


}
