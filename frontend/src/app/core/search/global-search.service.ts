import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type SearchResultType = 'client' | 'quote' | 'order';

export interface SearchResult {
  id: number;
  type: SearchResultType;
  title: string;
  subtitle: string;
  clientId: number;
}


@Injectable({
  providedIn: 'root'
})
export class GlobalSearchService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/search';

  search(term: string): Observable<SearchResult[]> {

    const params = new HttpParams()
      .set('q', term.trim())
      .set('limit', 10);

    return this.http.get<SearchResult[]>(
      this.apiUrl,
      { params }
    );
  }
}
