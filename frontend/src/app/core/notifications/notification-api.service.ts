import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppNotification, NotificationPage } from './notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/notifications';

  findAll(page = 0, size = 8): Observable<NotificationPage> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt,desc');
    return this.http.get<NotificationPage>(this.apiUrl, { params });
  }

  countUnread(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/unread/count`);
  }

  markAsRead(id: number): Observable<AppNotification> {
    return this.http.patch<AppNotification>(`${this.apiUrl}/${id}/read`, {});
  }

  dismiss(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
