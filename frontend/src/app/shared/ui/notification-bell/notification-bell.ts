import {
  Component, DestroyRef, HostListener, computed, inject, signal
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Subscription, catchError, timer } from 'rxjs';
import { NotificationApiService } from '../../../core/notifications/notification-api.service';
import { AppNotification } from '../../../core/notifications/notification.model';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './notification-bell.html',
  styleUrl: './notification-bell.scss'
})
export class NotificationBell {
  private readonly api = inject(NotificationApiService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private listRequest?: Subscription;
  private countRequest?: Subscription;

  readonly open = signal(false);
  readonly loading = signal(false);
  readonly busyId = signal<number | null>(null);
  readonly error = signal<string | null>(null);
  readonly items = signal<AppNotification[]>([]);
  readonly unreadCount = signal(0);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly visibleCount = computed(() => Math.min(this.unreadCount(), 99));

  constructor() {
    // Polling únicamente mientras el AppShell autenticado está montado.
    timer(0, 30000).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.refreshCount();
      if (this.open()) this.loadPage(this.page(), true);
    });

    this.destroyRef.onDestroy(() => {
      this.listRequest?.unsubscribe();
      this.countRequest?.unsubscribe();
    });
  }

  toggle(): void {
    if (this.open()) {
      this.open.set(false);
      return;
    }
    this.open.set(true);
    this.loadPage(0);
    this.refreshCount();
  }

  close(): void { this.open.set(false); }

  @HostListener('document:keydown.escape')
  onEscape(): void { this.close(); }

  private refreshCount(): void {
    this.countRequest?.unsubscribe();
    this.countRequest = this.api.countUnread().pipe(
      takeUntilDestroyed(this.destroyRef),
      catchError(() => EMPTY)
    ).subscribe(count => this.unreadCount.set(Math.max(0, Number(count) || 0)));
  }

  loadPage(page: number, silent = false): void {
    this.listRequest?.unsubscribe();
    if (!silent) this.loading.set(true);
    this.error.set(null);

    this.listRequest = this.api.findAll(page).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: result => {
        this.items.set(result.content);
        this.page.set(result.number);
        this.totalPages.set(result.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se han podido cargar las notificaciones.');
        this.loading.set(false);
      }
    });
  }

  navigate(notification: AppNotification): void {
    if (this.busyId() !== null) return;
    if (!notification.read) {
      this.busyId.set(notification.id);
      this.api.markAsRead(notification.id).pipe(
        takeUntilDestroyed(this.destroyRef)
      ).subscribe({
        next: () => {
          this.busyId.set(null);
          this.refreshCount();
          this.goToRelated(notification);
        },
        error: () => {
          this.busyId.set(null);
          this.error.set('No se ha podido marcar como leída.');
        }
      });
      return;
    }
    this.goToRelated(notification);
  }

  markRead(event: Event, notification: AppNotification): void {
    event.stopPropagation();
    if (notification.read || this.busyId() !== null) return;
    this.busyId.set(notification.id);
    this.api.markAsRead(notification.id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: updated => {
        this.items.update(list => list.map(item => item.id === updated.id ? updated : item));
        this.busyId.set(null);
        this.refreshCount();
      },
      error: () => {
        this.busyId.set(null);
        this.error.set('No se ha podido marcar como leída.');
      }
    });
  }

  dismiss(event: Event, notification: AppNotification): void {
    event.stopPropagation();
    if (this.busyId() !== null) return;
    this.busyId.set(notification.id);
    this.api.dismiss(notification.id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.busyId.set(null);
        this.refreshCount();
        this.loadPage(this.page());
      },
      error: () => {
        this.busyId.set(null);
        this.error.set('No se ha podido descartar la notificación.');
      }
    });
  }

  private goToRelated(notification: AppNotification): void {
    this.close();
    if (notification.followUpId != null && notification.quoteId != null) {
      // El listado reconoce quoteId.text y presenta los seguimientos del presupuesto.
      void this.router.navigate(['/followups'], {
        queryParams: { 'quoteId.text': String(notification.quoteId) }
      });
    } else if (notification.quoteId != null) {
      void this.router.navigate(['/quotes']);
    } else {
      void this.router.navigate(['/dashboard']);
    }
  }

  iconFor(type: AppNotification['type']): string {
    switch (type) {
      case 'FOLLOW_UP_DUE': return '◷';
      case 'FOLLOW_UP_OVERDUE': return '!';
      case 'QUOTE_EXPIRING': return '▤';
      case 'ORDER_READY': return '▣';
    }
  }
}
