import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Order,
  OrderSearch,
  OrderPage,
  OrderPayload
} from '../models/order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderApiService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/orders';

  search(filters: OrderSearch): Observable<OrderPage> {

    return this.http.post<OrderPage>(`${this.apiUrl}/search`, filters);
  }

  findById(id: number): Observable<Order> {

    return this.http.get<Order>(`${this.apiUrl}/${id}`);
  }

  create(payload: OrderPayload): Observable<Order> {

    return this.http.post<Order>(this.apiUrl, payload);
  }

  update(id: number, payload: OrderPayload): Observable<Order> {

    return this.http.put<Order>(`${this.apiUrl}/${id}`, payload);
  }

  confirm(id: number): Observable<Order> {

    return this.http.patch<Order>(`${this.apiUrl}/${id}/confirm`, {});
  }

  startPreparation(id: number): Observable<Order> {

    return this.http.patch<Order>(`${this.apiUrl}/${id}/start-preparation`, {});
  }

  markReady(id: number): Observable<Order> {

    return this.http.patch<Order>(`${this.apiUrl}/${id}/ready`, {});
  }

  deliver(id: number): Observable<Order> {

    return this.http.patch<Order>(`${this.apiUrl}/${id}/deliver`, {});
  }

  cancel(id: number): Observable<Order> {

    return this.http.patch<Order>(`${this.apiUrl}/${id}/cancel`, {});
  }

  createFromQuote(quoteId: number): Observable<Order> {

    return this.http.post<Order>(`${this.apiUrl}/from-quote/${quoteId}`, {});
  }
}
