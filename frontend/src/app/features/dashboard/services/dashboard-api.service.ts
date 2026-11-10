import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardSummary, DashboardCalendar } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardApiService {
  private readonly http = inject(HttpClient);

  calendar(year: number, month: number): Observable<DashboardCalendar> {
    return this.http.get<DashboardCalendar>('/api/dashboard/calendar', {
      params: { year: String(year), month: String(month) }
    });
  }

  summary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>('/api/dashboard/summary');
  }
}
