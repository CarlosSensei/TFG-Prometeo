import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CompleteFollowUpPayload, CreateFollowUpPayload,
  FollowUp, FollowUpPage, FollowUpSearch
} from '../models/followup.model';

@Injectable({ providedIn: 'root' })
export class FollowUpApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api';

  search(criteria: FollowUpSearch): Observable<FollowUpPage> {
    return this.http.post<FollowUpPage>(`${this.base}/followups/search`, criteria);
  }

  getById(id: number): Observable<FollowUp> {
    return this.http.get<FollowUp>(`${this.base}/followups/${id}`);
  }

  getByQuote(quoteId: number, page = 0, size = 10): Observable<FollowUpPage> {
    return this.http.get<FollowUpPage>(`${this.base}/quotes/${quoteId}/followups`, {
      params: { page, size, sort: 'scheduledAt,asc' }
    });
  }

  create(quoteId: number, payload: CreateFollowUpPayload): Observable<FollowUp> {
    return this.http.post<FollowUp>(`${this.base}/quotes/${quoteId}/followups`, payload);
  }

  complete(id: number, payload: CompleteFollowUpPayload): Observable<FollowUp> {
    return this.http.patch<FollowUp>(`${this.base}/followups/${id}/complete`, payload);
  }

  update(id: number, payload: FollowUp): Observable<FollowUp> {
    return this.http.put<FollowUp>(`${this.base}/followups/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/followups/${id}`);
  }
}
