import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Quote, QuotePage, QuoteSearch } from '../models/quote.model';
import { QuotePayload } from '../models/quote.model';

@Injectable({
  providedIn: 'root'
})
export class QuoteApiService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/quotes';

  search(filters: QuoteSearch): Observable<QuotePage> {
    
    return this.http.post<QuotePage>(`${this.apiUrl}/search`, filters);
  }

  getById(id: number): Observable<Quote> {

    return this.http.get<Quote>(`${this.apiUrl}/${id}`);
  }

  create(payload: QuotePayload): Observable<Quote> {

    return this.http.post<Quote>(this.apiUrl, payload);
  }

  update(id: number, payload: QuotePayload): Observable<Quote> {

    return this.http.put<Quote>(`${this.apiUrl}/${id}`, payload);
  }

  send(id: number): Observable<Quote> {

    return this.http.patch<Quote>(`${this.apiUrl}/${id}/send`, {});
  }

  accept(id: number): Observable<Quote> {

    return this.http.patch<Quote>(`${this.apiUrl}/${id}/accept`, {});
  }

  reject(id: number): Observable<Quote> {
    
    return this.http.patch<Quote>(`${this.apiUrl}/${id}/reject`, {});
  }

}
