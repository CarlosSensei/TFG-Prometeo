import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClientStats, ClientTimelineEvent } from '../models/client-commercial.model';
import { Client, ClientPayload } from '../models/client.model';
import { Page } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class ClientApiService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/clients';

  findAll(
    page = 0,
    size = 10,
    sort = 'surname,asc'
  ): Observable<Page<Client>> {

    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);

    return this.http.get<Page<Client>>(
      this.apiUrl,
      { params }
    );
  }

  findById(id: number): Observable<Client> {
    return this.http.get<Client>(
      `${this.apiUrl}/${id}`
    );
  }

  getById(id: number): Observable<Client> {
    return this.http.get<Client>(`${this.apiUrl}/${id}`);
  }

  create(client: ClientPayload): Observable<Client> {
    return this.http.post<Client>(this.apiUrl, client);
  }

  update(id: number, client: ClientPayload): Observable<Client> {
    return this.http.put<Client>(`${this.apiUrl}/${id}`, client);
  }

  getStats(id: number): Observable<ClientStats> {
    return this.http.get<ClientStats>(
      `${this.apiUrl}/${id}/stats`
    );
  }

  getTimeline(id: number): Observable<ClientTimelineEvent[]> {
    return this.http.get<ClientTimelineEvent[]>(
      `${this.apiUrl}/${id}/timeline`
    );
  }

}
