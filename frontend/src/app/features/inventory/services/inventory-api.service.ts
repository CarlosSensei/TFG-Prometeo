import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InventoryItem, InventoryPage, InventoryPayload, InventorySearch } from '../models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/inventory';

  search(query: InventorySearch): Observable<InventoryPage> {
    return this.http.post<InventoryPage>(`${this.baseUrl}/search`, query);
  }

  findById(id: number): Observable<InventoryItem> {
    return this.http.get<InventoryItem>(`${this.baseUrl}/${id}`);
  }

  create(payload: InventoryPayload): Observable<InventoryItem> {
    return this.http.post<InventoryItem>(this.baseUrl, payload);
  }

  update(id: number, payload: InventoryPayload): Observable<InventoryItem> {
    return this.http.put<InventoryItem>(`${this.baseUrl}/${id}`, payload);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
