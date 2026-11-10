import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateStockMovement, StockMovement, StockMovementPage } from '../models/stock-movement.model';

@Injectable({ providedIn: 'root' })
export class StockMovementApiService {
  private readonly http = inject(HttpClient);

  findByItem(itemId: number, page = 0, size = 10): Observable<StockMovementPage> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<StockMovementPage>(`/api/inventory/${itemId}/movements`, { params });
  }

  create(itemId: number, payload: CreateStockMovement): Observable<StockMovement> {
    return this.http.post<StockMovement>(`/api/inventory/${itemId}/movements`, payload);
  }
}
