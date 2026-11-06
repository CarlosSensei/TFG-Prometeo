import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Client } from '../../clients/models/client.model';
import { Page } from '../../clients/models/page.model';

export interface InventoryItem {
  id: number;
  name: string;
  sku: string;
  brand: string | null;
  salePrice: number;
  taxRate: number;
  stock: number;
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class QuoteLookupService {

  private readonly http = inject(HttpClient);

  searchClients(name: string): Observable<Page<Client>> {

    return this.http.post<Page<Client>>(
      '/api/clients/search',
      {
        name: name.trim(),
        active: true,
        page: 0,
        size: 20,
        sortBy: 'surname',
        sortDirection: 'asc'
      }
    );
  }

  getClient(id: number): Observable<Client> {
    
    return this.http.get<Client>(`/api/clients/${id}`);
  }

  searchInventory(name: string): Observable<Page<InventoryItem>> {

    return this.http.post<Page<InventoryItem>>(
      '/api/inventory/search',
      {
        name: name.trim(),
        active: true,
        page: 0,
        size: 20,
        sortBy: 'name',
        sortDirection: 'asc'
      }
    );
  }

  getInventoryItem(id: number): Observable<InventoryItem> {

    return this.http.get<InventoryItem>(`/api/inventory/${id}`);
  }
}
